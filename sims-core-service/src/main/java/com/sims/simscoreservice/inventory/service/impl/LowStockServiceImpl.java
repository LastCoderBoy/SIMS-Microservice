package com.sims.simscoreservice.inventory.service.impl;

import com.sims.common.exceptions.ServiceException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryResponse;
import com.sims.simscoreservice.inventory.dto.lowStock.LowStockMetrics;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.helper.InventoryHelper;
import com.sims.simscoreservice.inventory.service.LowStockService;
import com.sims.simscoreservice.inventory.queryService.InventoryQueryService;
import com.sims.simscoreservice.inventory.searchService.InventorySearchService;
import com.sims.simscoreservice.product.enums.ProductCategories;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Low Stock Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LowStockServiceImpl implements LowStockService {

    private final InventoryQueryService inventoryQueryService;
    private final InventorySearchService inventorySearchService;
    private final InventoryHelper inventoryHelper;

    @Override
    @Transactional(readOnly = true)
    public LowStockMetrics getLowStockDashboardMetrics(){
        try{
            return inventoryQueryService.getLowStockMetrics();
        } catch (Exception e) {
            log.error("[LOW-STOCK-SERVICE] Error retrieving low stock metrics: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve low stock metrics", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InventoryResponse> getAllLowStockProducts(String sortBy, String sortDirection, int page, int size) {
        try {
            Page<Inventory> lowStockPage = inventoryQueryService.getAllLowStockProducts(sortBy, sortDirection, page, size);

            log.info("[LOW-STOCK-SERVICE] Retrieved {} low stock products", lowStockPage.getTotalElements());

            return inventoryHelper.toPaginatedResponse(lowStockPage);

        } catch (Exception e) {
            log.error("[LOW-STOCK-SERVICE] Error retrieving low stock products: {}", e.getMessage());
            throw new ServiceException("Failed to retrieve low stock products", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InventoryResponse> searchLowStockProducts(String text, int page, int size, String sortBy, String sortDirection) {
        try {
            Page<Inventory> searchResults = inventorySearchService.searchInLowStockProducts(text, page, size, sortBy, sortDirection);

            log.info("[LOW-STOCK-SERVICE] Search returned {} results", searchResults.getTotalElements());

            return inventoryHelper.toPaginatedResponse(searchResults);

        } catch (Exception e) {
            log.error("[LOW-STOCK-SERVICE] Error searching low stock products: {}", e.getMessage());
            throw new ServiceException("Failed to search low stock products", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<InventoryResponse> filterLowStockProducts(ProductCategories category, String sortBy,
                                                                       String sortDirection, int page, int size) {
        try {
            Page<Inventory> filterResults = inventorySearchService.filterLowStockProducts(category, sortBy, sortDirection, page, size);

            log.info("[LOW-STOCK-SERVICE] Filter returned {} results", filterResults.getTotalElements());

            return inventoryHelper.toPaginatedResponse(filterResults);

        } catch (Exception e) {
            log.error("[LOW-STOCK-SERVICE] Error filtering low stock products: {}", e.getMessage());
            throw new ServiceException("Failed to filter low stock products", e);
        }
    }

    @Override
    public void generateLowStockReport(HttpServletResponse response, String sortBy, String sortDirection) {
        try {
            // Get all low stock products (no pagination)
            List<Inventory> lowStockProducts = inventoryQueryService.getAllLowStockProducts(sortBy, sortDirection);

            // Generate Excel report
            inventoryHelper.generateExcelReport(lowStockProducts, response);

            log.info("[LOW-STOCK-SERVICE] Generated low stock report for {} products", lowStockProducts.size());

        } catch (Exception e) {
            log.error("[LOW-STOCK-SERVICE] Error generating low stock report: {}", e.getMessage());
            throw new ServiceException("Failed to generate low stock report", e);
        }
    }
}
