package com.sims.simscoreservice.inventory.controller;


import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryResponse;
import com.sims.simscoreservice.inventory.dto.lowStock.LowStockMetrics;
import com.sims.simscoreservice.inventory.service.LowStockService;
import com.sims.simscoreservice.product.enums.ProductCategories;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sims.common.constants.AppConstants.*;

/**
 * Low Stock Controller
 * Manages low stock inventory items
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(BASE_INVENTORY_PATH + "/low-stock")
public class LowStockController {

    private final LowStockService lowStockService;

    @GetMapping
    public ResponseEntity<ApiResponse<LowStockMetrics>> getLowStockDashboardMetrics(){
        log.info("[LOW-STOCK-CONTROLLER] Get low stock dashboard metrics");
        return ResponseEntity
                .ok(ApiResponse.success(
                        "Low stock dashboard metrics retrieved successfully",
                        lowStockService.getLowStockDashboardMetrics())
        );
    }

    /**
     * Get all low stock products with pagination
     * Returns products where sku <= minLevel
     *
     * @param sortBy field to sort by (default: sku)
     * @param sortDirection asc or desc (default: asc)
     * @param page page number
     * @param size page size
     * @return PaginatedResponse with low stock products
     */
    @GetMapping("/all")
    public ResponseEntity<PaginatedResponse<InventoryResponse>> getAllLowStockProducts(
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[LOW-STOCK-CONTROLLER] Get all low stock products by user: {}", userId);

        PaginatedResponse<InventoryResponse> response =
                lowStockService.getAllLowStockProducts(sortBy, sortDirection, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * Search low stock products
     * Searches by: SKU, Location, Product ID, Product Name, Category
     * Only searches within low stock items
     *
     * @param text search text
     * @return PaginatedResponse with search results
     */
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<InventoryResponse>> searchLowStockProducts(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[LOW-STOCK-CONTROLLER] Search low stock products with text '{}' by user: {}", text, userId);

        PaginatedResponse<InventoryResponse> response =
                lowStockService.searchLowStockProducts(text, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    /**
     * Filter low stock products by category
     * Uses ProductCategories enum with converter
     *
     * @param category ProductCategories enum (EDUCATION, ELECTRONIC, etc.)
     * @return PaginatedResponse with filtered results
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<InventoryResponse>> filterLowStockProducts(
            @RequestParam ProductCategories category,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[LOW-STOCK-CONTROLLER] Filter low stock by category '{}' by user: {}", category, userId);

        PaginatedResponse<InventoryResponse> response =
                lowStockService.filterLowStockProducts(category, sortBy, sortDirection, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * Generate Excel report for low stock products
     * Exports all low stock products to Excel file
     *
     * @param response HTTP response
     * @param sortBy field to sort by
     * @param sortDirection asc or desc
     */
    @GetMapping("/report")
    public void generateLowStockReport(
            HttpServletResponse response,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[LOW-STOCK-CONTROLLER] Generate low stock report by user: {}", userId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=LowStockReport.xlsx");

        lowStockService.generateLowStockReport(response, sortBy, sortDirection);
    }
}
