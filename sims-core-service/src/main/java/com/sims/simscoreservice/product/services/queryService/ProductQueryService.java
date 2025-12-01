package com.sims.simscoreservice.product.services.queryService;


import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.product.dto.ProductReportMetrics;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.product.repository.ProductRepository;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Shared query service for product-related read operations
 * Purpose: Break circular dependencies between ProductManagementService and other services
 * Contains ONLY read operations - no business logic or state changes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final GlobalServiceHelper globalServiceHelper;

    /**
     * Find product by ID - throws exception if not found
     * Used by: SalesOrderService, PurchaseOrderService, InventoryService
     */
    @Transactional(readOnly = true)
    public Product findById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll(Sort.by("productId").ascending());
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(String sortBy, String sortDirection, int page, int size) {
        try {
            Pageable pageable = globalServiceHelper.preparePageable(page, size, sortBy, sortDirection);
            return productRepository.findAll(pageable);
        } catch (DataAccessException e) {
            log.error("PM (getAllProducts): Failed to retrieve products due to database error: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve products due to database error", e);
        } catch (Exception e) {
            log.error("PM (getAllProducts): Failed to retrieve products: {}", e.getMessage());
            throw new ServiceException("Internal Service Error occurred", e);
        }
    }

    @Transactional(readOnly = true)
    public Product isProductFinalized(String productId) throws ResourceNotFoundException, ValidationException {
        Product product = findById(productId);
        if (product.isInInvalidStatus()) {
            throw new ValidationException("Product is not for sale and cannot be ordered. Please update the status in the PM section first.");
        }
        return product;
    }


    /**
     * Helper method for Report & Analytics section
     * @return ProductReportMetrics object containing counts of active and inactive products
     */
    @Transactional(readOnly = true)
    public ProductReportMetrics countTotalActiveInactiveProducts(){
        try {
            return productRepository.getProductReportMetrics(
                    ProductStatus.getActiveStatuses(),
                    ProductStatus.getInactiveStatuses()
            );
        } catch (DataAccessException e) {
            log.error("PM (totalProductsByStatus): Failed to retrieve product metrics due to database error: {}", e.getMessage());
            throw new DatabaseException("PM (totalProductsByStatus): Failed to retrieve product metrics", e);
        } catch (Exception e) {
            log.error("PM (totalProductsByStatus): Failed to retrieve product metrics: {}", e.getMessage());
            throw new ServiceException("Internal Service Error occurred", e);
        }
    }
}
