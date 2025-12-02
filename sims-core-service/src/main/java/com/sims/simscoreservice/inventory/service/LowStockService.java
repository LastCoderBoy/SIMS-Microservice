package com.sims.simscoreservice.inventory.service;


import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryResponse;
import com.sims.simscoreservice.product.enums.ProductCategories;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Low Stock Service Interface
 * Handles low stock inventory items
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface LowStockService {

    PaginatedResponse<InventoryResponse> getAllLowStockProducts(String sortBy, String sortDirection, int page, int size);

    PaginatedResponse<InventoryResponse> searchLowStockProducts(String text, int page, int size, String sortBy, String sortDirection);

    /**
     * Filter low stock products by Product Category
     */
    PaginatedResponse<InventoryResponse> filterLowStockProducts(ProductCategories category, String sortBy, String sortDirection, int page, int size);

    void generateLowStockReport(HttpServletResponse response, String sortBy, String sortDirection);
}
