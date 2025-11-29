package com.sims.simscoreservice.inventory.controller;


import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryRequest;
import com.sims.simscoreservice.inventory.dto.InventoryResponse;
import com.sims.simscoreservice.inventory.service.TotalItemsService;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sims.common.constants.AppConstants.*;

/**
 * Total Items Controller
 * Manages all inventory products (CRUD operations)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_INVENTORY_PATH + "/total")
@RequiredArgsConstructor
@Slf4j
public class TotalItemsController {

    private final TotalItemsService totalItemsService;
    private final RoleValidator roleValidator;

    /**
     * Get all inventory products with pagination
     *
     * @param sortBy field to sort by (default: sku)
     * @param sortDirection asc or desc (default: desc)
     * @param page page number
     * @param size page size
     * @return PaginatedResponse with inventory products
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<InventoryResponse>> getAllProducts(
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[TOTAL-ITEMS-CONTROLLER] Get all products by user: {}", userId);

        PaginatedResponse<InventoryResponse> inventoryResponse =
                totalItemsService.getAllInventoryProducts(sortBy, sortDirection, page, size);

        return ResponseEntity.ok(inventoryResponse);
    }

    /**
     * Update inventory stock levels (current stock and min level)
     * Only ADMIN/MANAGER can update
     *
     * @param sku Stock Keeping Unit
     * @param request InventoryRequest with currentStock and minLevel
     * @return ApiResponse<Void>
     */
    @PutMapping("/{sku}/update")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable String sku,
            @Valid @RequestBody InventoryRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) throws BadRequestException {

        log.info("[TOTAL-ITEMS-CONTROLLER] Update inventory {} by user: {}", sku, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        // Validate SKU
        if (sku == null || sku.trim().isEmpty()) {
            throw new BadRequestException("SKU cannot be null or empty");
        }

        ApiResponse<Void> response = totalItemsService.updateInventoryStockLevels(sku. toUpperCase(), request);

        return ResponseEntity.ok(response);
    }

    /**
     * Search inventory products
     * Searches by: SKU, Location, Product ID, Product Name, Category
     *
     * @param text search text
     * @return PaginatedResponse with search results
     */
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<InventoryResponse>> searchProduct(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[TOTAL-ITEMS-CONTROLLER] Search inventory with text '{}' by user: {}", text, userId);

        PaginatedResponse<InventoryResponse> inventoryResponse =
                totalItemsService.searchInventoryProducts(text, sortBy, sortDirection, page, size);

        return ResponseEntity.ok(inventoryResponse);
    }

    /**
     * Filter inventory products
     * Supports:
     * - status:IN_STOCK (Inventory status)
     * - status:LOW_STOCK
     * - stock:50 (current stock <= 50)
     * - ELECTRONIC (product category)
     *
     * @param filterBy filter criteria
     * @return PaginatedResponse with filtered results
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<InventoryResponse>> filterProducts(
            @RequestParam String filterBy,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[TOTAL-ITEMS-CONTROLLER] Filter inventory with '{}' by user: {}", filterBy, userId);

        PaginatedResponse<InventoryResponse> filterResponse =
                totalItemsService.filterInventoryProducts(filterBy, sortBy, sortDirection, page, size);

        return ResponseEntity.ok(filterResponse);
    }

    /**
     * Delete inventory product
     * - Removes from inventory
     * - Archives product in Product Management
     * Only ADMIN can delete
     *
     * @param sku Stock Keeping Unit
     * @return ApiResponse<Void>
     */
    @DeleteMapping("/{sku}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String sku,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) throws BadRequestException {

        log.info("[TOTAL-ITEMS-CONTROLLER] Delete inventory {} by user: {}", sku, userId);

        // Check authorization (only ADMIN)
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN");

        // Validate SKU
        if (sku == null || sku.trim().isEmpty()) {
            throw new BadRequestException("SKU cannot be empty");
        }

        ApiResponse<Void> response = totalItemsService.deleteInventoryProduct(sku.toUpperCase());

        return ResponseEntity.ok(response);
    }

    /**
     * Generate Excel report for all inventory products
     *
     * @param response HTTP response
     * @param sortBy field to sort by
     * @param sortDirection asc or desc
     */
    @GetMapping("/report")
    public void generateReport(
            HttpServletResponse response,
            @RequestParam(defaultValue = "sku") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[TOTAL-ITEMS-CONTROLLER] Generate inventory report by user: {}", userId);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=TotalItemsReport.xlsx");

        totalItemsService.generateInventoryReport(response, sortBy, sortDirection);
    }
}
