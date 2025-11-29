package com.sims.simscoreservice.inventory.service;

import com.sims.common.models.ApiResponse;
import com. sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryRequest;
import com.sims.simscoreservice.inventory.dto.InventoryResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Total Items Service Interface
 * Handles all inventory products (CRUD operations)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface TotalItemsService {

    PaginatedResponse<InventoryResponse> getAllInventoryProducts(String sortBy, String sortDirection, int page, int size);
    ApiResponse<Void> updateInventoryStockLevels(String sku, InventoryRequest request);
    PaginatedResponse<InventoryResponse> searchInventoryProducts(String text, String sortBy, String sortDirection, int page, int size);
    PaginatedResponse<InventoryResponse> filterInventoryProducts(String filterBy, String sortBy, String sortDirection, int page, int size);
    ApiResponse<Void> deleteInventoryProduct(String sku);
    void generateInventoryReport(HttpServletResponse response, String sortBy, String sortDirection);
}
