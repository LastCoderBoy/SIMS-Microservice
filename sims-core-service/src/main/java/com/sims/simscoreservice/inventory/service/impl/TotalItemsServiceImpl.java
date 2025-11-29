package com.sims.simscoreservice.inventory.service.impl;


import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryRequest;
import com.sims.simscoreservice.inventory.dto.InventoryResponse;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.helper.InventoryHelper;
import com.sims.simscoreservice.inventory.mapper.InventoryMapper;
import com.sims.simscoreservice.inventory.repository.InventoryRepository;
import com.sims.simscoreservice.inventory.service.TotalItemsService;
import com.sims.simscoreservice.inventory.service.queryService.InventoryQueryService;
import com.sims.simscoreservice.inventory.service.searchService.InventorySearchService;
import com.sims.simscoreservice.inventory.service.stockManagement.StockManagementService;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.product.services.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Total Items Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TotalItemsServiceImpl implements TotalItemsService {

    // ========== Services ==========
    private final InventoryQueryService inventoryQueryService;
    private final InventorySearchService inventorySearchService;
    private final StockManagementService stockManagementService;
    private final ProductService productService;

    // ========== Components ==========
    private final InventoryHelper inventoryHelper;

    // ========== Repositories ==========
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InventoryResponse> getAllInventoryProducts(String sortBy, String sortDirection, int page, int size) {
        try {
            Page<Inventory> inventoryPage = inventoryQueryService.getAllInventoryProducts(sortBy, sortDirection, page, size);

            log.info("[TOTAL-ITEMS] Retrieved {} inventory products", inventoryPage. getTotalElements());

            return inventoryHelper.toPaginatedResponse(inventoryPage);

        } catch (Exception e) {
            log.error("[TOTAL-ITEMS] Error retrieving inventory products: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve inventory products", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> updateInventoryStockLevels(String sku, InventoryRequest request) {
        try {
            // Validate request
            inventoryHelper.validateUpdateRequest(request);

            // Find existing inventory
            Inventory inventory = inventoryQueryService.getInventoryBySku(sku);

            // Update stock levels
            stockManagementService.updateStockLevels(
                    inventory,
                    request.getCurrentStock(),
                    request.getMinLevel()
            );

            log.info("[TOTAL-ITEMS] Inventory {} updated successfully", sku);

            return ApiResponse.success("Inventory " + sku + " updated successfully");

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (DataAccessException da) {
            log.error("[TOTAL-ITEMS] Database error updating inventory {}: {}", sku, da. getMessage());
            throw new DatabaseException("Failed to update inventory", da);
        } catch (Exception e) {
            log.error("[TOTAL-ITEMS] Error updating inventory {}: {}", sku, e.getMessage());
            throw new ServiceException("Failed to update inventory", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InventoryResponse> searchInventoryProducts(String text, String sortBy, String sortDirection, int page, int size) {
        try {
            Page<Inventory> searchResults = inventorySearchService.searchAll(text, sortBy, sortDirection, page, size);

            log.info("[TOTAL-ITEMS] Search returned {} results", searchResults.getTotalElements());

            return inventoryHelper.toPaginatedResponse(searchResults);

        } catch (Exception e) {
            log.error("[TOTAL-ITEMS] Error searching inventory: {}", e.getMessage());
            throw new ServiceException("Failed to search inventory", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InventoryResponse> filterInventoryProducts(String filterBy, String sortBy, String sortDirection, int page, int size) {
        try {
            Page<Inventory> filterResults = inventorySearchService.filterAll(filterBy, sortBy, sortDirection, page, size);

            log.info("[TOTAL-ITEMS] Filter returned {} results", filterResults.getTotalElements());

            return inventoryHelper.toPaginatedResponse(filterResults);

        } catch (Exception e) {
            log.error("[TOTAL-ITEMS] Error filtering inventory: {}", e.getMessage());
            throw new ServiceException("Failed to filter inventory", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteInventoryProduct(String sku) {
        try {
            // Find inventory
            Inventory inventory = inventoryQueryService.getInventoryBySku(sku);
            Product product = inventory.getProduct();

            // Archive product in PM if it's active
            if (product. getStatus() == ProductStatus. ACTIVE ||
                    product.getStatus() == ProductStatus.PLANNING ||
                    product.getStatus() == ProductStatus.ON_ORDER) {

                product.setStatus(ProductStatus.ARCHIVED);
                productService.saveProduct(product);

                log.info("[TOTAL-ITEMS] Product {} archived in PM", product.getProductId());
            }

            // Delete from inventory
            inventoryRepository.delete(inventory);

            log.info("[TOTAL-ITEMS] Inventory {} deleted successfully", sku);

            return ApiResponse.success("Product " + sku + " deleted successfully");

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (DataAccessException de) {
            log.error("[TOTAL-ITEMS] Database error deleting inventory {}: {}", sku, de.getMessage());
            throw new DatabaseException("Failed to delete inventory", de);
        } catch (Exception e) {
            log.error("[TOTAL-ITEMS] Error deleting inventory {}: {}", sku, e.getMessage());
            throw new ServiceException("Failed to delete inventory", e);
        }
    }

    @Override
    public void generateInventoryReport(HttpServletResponse response, String sortBy, String sortDirection) {
        try {
            List<Inventory> inventoryProducts = inventoryQueryService.getAllInventoryProducts(sortBy, sortDirection);

            inventoryHelper.generateExcelReport(inventoryProducts, response);

            log.info("[TOTAL-ITEMS] Generated report for {} products", inventoryProducts. size());

        } catch (Exception e) {
            log.error("[TOTAL-ITEMS] Error generating report: {}", e.getMessage());
            throw new ServiceException("Failed to generate report", e);
        }
    }
}
