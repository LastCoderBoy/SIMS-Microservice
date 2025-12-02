package com.sims.simscoreservice.product.services.impl;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.inventory.service.InventoryService;
import com.sims.simscoreservice.inventory.queryService.InventoryQueryService;
import com.sims.simscoreservice.orderManagement.salesOrder.service.queryService.SalesOrderQueryService;
import com.sims.simscoreservice.product.dto.BatchProductRequest;
import com.sims.simscoreservice.product.dto.BatchProductResponse;
import com.sims.simscoreservice.product.dto.ProductRequest;
import com.sims.simscoreservice.product.dto.ProductResponse;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.product.mapper.ProductMapper;
import com.sims.simscoreservice.product.repository.ProductRepository;
import com.sims.simscoreservice.product.services.ProductService;
import com.sims.simscoreservice.product.helper.ProductHelper;
import com.sims.simscoreservice.product.services.queryService.ProductQueryService;
import com.sims.simscoreservice.product.services.searchService.ProductSearchService;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Product Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    // ========== Components ==========
    private final ProductMapper productMapper;
    private final ProductHelper productHelper;

    // ========== Services ==========
    private final ProductQueryService productQueryService;
    private final ProductSearchService productSearchService;
    private final InventoryQueryService inventoryQueryService;
    private final InventoryService inventoryService;
    private final SalesOrderQueryService salesOrderQueryService;

    // ========== Repositories ==========
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> getAllProducts(String sortBy, String sortDirection, int page, int size) {
        // Delegate to query service
        Page<Product> productPage =
                productQueryService.getAllProducts(sortBy, sortDirection, page, size);

        log.info("[PRODUCT-SERVICE] Retrieved {} products from database", productPage.getTotalElements());
        return productHelper.toPaginatedResponse(productPage);
    }

    @Override
    @Transactional
    public ProductResponse addProduct(ProductRequest request, String userId) {
        try {
            // Validate product & Convert to entity
            productHelper.validateProduct(request);
            Product product = productMapper.toEntity(request);
            product.setProductId(generateProductId());

            // Save product
            Product savedProduct = productRepository.save(product);
            productRepository.flush(); // This populates @CreationTimestamp

            // Add to inventory if status is not PLANNING
            if (!request.getStatus().equals(ProductStatus.PLANNING)) {
                inventoryService.addProduct(savedProduct, false);
                log.info("[PRODUCT-SERVICE] Product {} added to inventory", savedProduct.getProductId());
            }

            log.info("[PRODUCT-SERVICE] Product added - ID: {}, Name: {}, By: {}",
                    savedProduct.getProductId(), savedProduct.getName(), userId);
            return productMapper.toResponse(savedProduct);

        } catch (ValidationException ve) {
            throw ve;
        } catch (DataIntegrityViolationException e) {
            log.error("[PRODUCT-SERVICE] Duplicate product: {}", e.getMessage());
            throw new ValidationException("Product with this name already exists");
        } catch (Exception e) {
            log.error("[PRODUCT-SERVICE] Failed to add product: {}", e.getMessage(), e);
            throw new ServiceException("Failed to add product", e);
        }
    }

    @Override
    @Transactional
    public BatchProductResponse addProductsBatch(BatchProductRequest request, String userId) {
        log.info("[PRODUCT-SERVICE] Processing batch of {} products by user: {}",
                request.getProducts().size(), userId);

        List<String> successfulIds = new ArrayList<>();
        List<BatchProductResponse.ProductError> errors = new ArrayList<>();

        for (int i = 0; i < request.getProducts().size(); i++) {
            ProductRequest productRequest = request.getProducts().get(i);
            try {
                productHelper.validateProduct(productRequest);

                Product product = productMapper.toEntity(productRequest);
                product.setProductId(generateProductId());
                Product savedProduct = productRepository.save(product);

                // Add to inventory if needed
                if (!productRequest.getStatus().equals(ProductStatus.PLANNING)) {
                    inventoryService.addProduct(savedProduct, false);
                }

                successfulIds.add(savedProduct.getProductId());
                log.debug("[PRODUCT-SERVICE] Product {} added successfully", savedProduct.getProductId());

            } catch (Exception e) {
                log.warn("[PRODUCT-SERVICE] Failed to add product at index {} - {}", i, e.getMessage());
                errors.add(new BatchProductResponse.ProductError(i, productRequest, e.getMessage()));
            }
        }

        BatchProductResponse response = BatchProductResponse.builder()
                .totalRequested(request.getProducts().size())
                .successCount(successfulIds.size())
                .failureCount(errors.size())
                .successfulProductIds(successfulIds)
                .errors(errors)
                .build();

        log.info("[PRODUCT-SERVICE] Batch completed - {}/{} successful",
                response.getSuccessCount(), response.getTotalRequested());
        return response;
    }

    @Override
    public ApiResponse<Void> updateProduct(String productId, ProductRequest request, String userId) {
        try {
            Product currentProduct = productQueryService.findById(productId);
            if (productHelper.isAllFieldsNull(request)) {
                log.info("[PRODUCT-SERVICE] No fields to update for product {}", productId);
                return ApiResponse. error("No fields to update");
            }

            // Update basic product fields
            productMapper.updateEntityFromRequest(request, currentProduct);

            // Handle status change (complex logic with inventory sync)
            if (request.getStatus() != null && ! request.getStatus().equals(currentProduct.getStatus())) {
                updateProductAndInventoryStatus(currentProduct, request.getStatus(), productId);
            }

            // Save updated product
            productRepository.save(currentProduct);

            log.info("[PRODUCT-SERVICE] Product {} updated by user: {}", productId, userId);
            return ApiResponse.success("Product with ID " + productId + " updated successfully!");

        } catch (ResourceNotFoundException e) {
            log.error("[PRODUCT-SERVICE] Product {} not found: {}", productId, e.getMessage());
            throw e;
        } catch (ValidationException e) {
            log.error("[PRODUCT-SERVICE] Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[PRODUCT-SERVICE] Failed to update product {}: {}", productId, e. getMessage(), e);
            throw new ServiceException("Failed to update product", e);
        }
    }

    /**
     * Handle product status change and inventory synchronization
     * This method handles complex status transitions:
     * - PLANNING → ACTIVE/ON_ORDER: Add to inventory
     * - ARCHIVED → ACTIVE/ON_ORDER: Add to inventory (if not already present)
     * - Any status → INVALID status: Mark inventory as invalid
     */
    private void updateProductAndInventoryStatus(Product currentProduct, ProductStatus newStatus, String productId) {
        ProductStatus currentStatus = currentProduct.getStatus();
        if (newStatus != null && !newStatus.equals(currentStatus)) {

            Optional<Inventory> productInInventory =
                    inventoryQueryService.getInventoryByProductId(productId);

            if (productHelper.validateStatusBeforeAdding(currentStatus, newStatus)) {
                currentProduct.setStatus(newStatus);
                handleStatusChange(currentProduct, newStatus, currentStatus, productId);
            } else {
                currentProduct.setStatus(newStatus);

                // If changing to invalid status, mark inventory as invalid
                if (GlobalServiceHelper.amongInvalidStatus(newStatus)) {
                    inventoryService.updateInventoryStatus(productInInventory, InventoryStatus.INVALID);
                    log.info("[PRODUCT-SERVICE] Product {} inventory marked as INVALID due to status {}",
                            productId, newStatus);
                }
            }
        }
    }

    private void handleStatusChange(Product currentProduct, ProductStatus newStatus,
                                    ProductStatus previousStatus, String productId) {


        Optional<Inventory> productInInventory = inventoryQueryService.getInventoryByProductId(productId);

        if (newStatus == ProductStatus.ACTIVE || newStatus == ProductStatus.ON_ORDER) {
            if (previousStatus == ProductStatus. ARCHIVED) {
                // Status: ARCHIVED → ACTIVE/ON_ORDER
                // Only add to inventory if not already present

                 if (productInInventory.isEmpty()) {
                     inventoryService.addProduct(currentProduct, false);
                     log.info("[PRODUCT-SERVICE] Product {} added to inventory (was ARCHIVED)", productId);
                 } else {
                     log.info("[PRODUCT-SERVICE] Product {} already in inventory, skipping addition", productId);
                 }

                log.info("[PRODUCT-SERVICE] Status change: ARCHIVED → {} for product {}", newStatus, productId);

            } else {
                // Status: PLANNING → ACTIVE/ON_ORDER
                // Always add to inventory
                inventoryService.addProduct(currentProduct, false);
                log.info("[PRODUCT-SERVICE] Product {} added to inventory due to status change: {} → {}",
                        productId, previousStatus, newStatus);
            }
        } else {
            // Status: Any → INVALID status (RESTRICTED, ARCHIVED, DISCONTINUED)
            // Mark inventory as invalid (don't delete, just mark)
            inventoryService.updateInventoryStatus(productInInventory, InventoryStatus.INVALID);
            log. info("[PRODUCT-SERVICE] Product {} inventory marked as INVALID", productId);
        }
    }

    @Override
    public ApiResponse<Void> deleteProduct(String productId, String userId) {
        try {
            // Find product using QueryService
            Product product = productQueryService.findById(productId);

            // Check if product is used in active orders

             long activeOrdersCount = salesOrderQueryService.countActiveOrdersForProduct(productId);
             if (activeOrdersCount > 0) {
                 throw new ValidationException(
                     "Cannot delete product with " + activeOrdersCount + " active orders. Cancel orders first."
                 );
             }

            // Delete from inventory first
            inventoryService.deleteByProductId(productId);

            // Delete product
            productRepository.delete(product);

            log.info("[PRODUCT-SERVICE] Product {} deleted by user: {}", productId, userId);
            return ApiResponse.success("Product deleted successfully");

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[PRODUCT-SERVICE] Database error deleting product: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to delete product", e);
        } catch (Exception e) {
            log.error("[PRODUCT-SERVICE] Failed to delete product: {}", e.getMessage(), e);
            throw new ServiceException("Failed to delete product", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> searchProducts(String text, String sortBy, String sortDirection, int page, int size) {
        log.info("[PRODUCT-SERVICE] Search products with text: '{}'", text);

        Page<Product> searchResult =
                productSearchService.searchProduct(text, sortBy, sortDirection, page, size);

        log.info("[PRODUCT-SERVICE] Search returned {} results", searchResult.getTotalElements());
        return productHelper.toPaginatedResponse(searchResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> filterProducts(String filter, String sortBy, String direction, int page, int size) {
        log.info("[PRODUCT-SERVICE] Filter products with: '{}'", filter);

        Page<Product> filterResult =
                productSearchService.filterProducts(filter, sortBy, direction, page, size);

        log.info("[PRODUCT-SERVICE] Filter returned {} results", filterResult.getTotalElements());
        return productHelper.toPaginatedResponse(filterResult);
    }

    @Override
    public void generateProductReport(HttpServletResponse response) {
        try {
            List<Product> products = productQueryService.getAllProducts();
            productHelper.generateExcelReport(products, response);

            log.info("[PRODUCT-SERVICE] Generated report for {} products", products.size());

        } catch (Exception e) {
            log. error("[PRODUCT-SERVICE] Failed to generate report: {}", e. getMessage(), e);
            throw new ServiceException("Failed to generate report", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveProduct(Product product) {
        try {
            productRepository.save(product);
            log.info("[PRODUCT-SERVICE] Saved product: {}", product.getProductId());
        } catch (DataAccessException e) {
            log.error("[PRODUCT-SERVICE] Database error saving product: {}", e.getMessage());
            throw new DatabaseException("Failed to save product", e);
        } catch (Exception e) {
            log.error("[PRODUCT-SERVICE] Failed to save product: {}", e.getMessage());
            throw new ServiceException("Failed to save product", e);
        }
    }

    /**
     * Generate unique product ID (PRD001, PRD002, ...)
     */
    @Transactional(readOnly = true)
    public String generateProductId() {
        return productRepository.findLastProductId()
                .map(lastId -> {
                    int lastNumber = Integer.parseInt(lastId.substring(3));
                    return String.format("PRD%03d", lastNumber + 1);
                })
                .orElse("PRD001");
    }
}
