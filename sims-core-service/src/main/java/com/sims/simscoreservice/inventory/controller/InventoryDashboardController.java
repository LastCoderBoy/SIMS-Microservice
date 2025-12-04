package com.sims.simscoreservice.inventory.controller;

import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryPageResponse;
import com.sims.simscoreservice.inventory.dto.PendingOrderResponse;
import com.sims.simscoreservice.inventory.service.InventoryService;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.sims.common.constants.AppConstants.*;

/**
 * Inventory Dashboard Controller
 * Main controller for inventory management dashboard
 * Handles pending orders (Sales Orders + Purchase Orders)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_INVENTORY_PATH)
@RequiredArgsConstructor
@Slf4j
public class InventoryDashboardController {

    private final InventoryService inventoryService;

    /**
     * Get Inventory Dashboard Page Data
     * Returns: Total inventory, low stock count, incoming/outgoing stock, damage/loss, pending orders
     *
     * @param page page number (default: 0)
     * @param size page size (default: 10)
     * @return InventoryPageResponse with dashboard metrics and pending orders
     */
    @GetMapping
    public ResponseEntity<InventoryPageResponse> getInventoryControlPageData(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {

        log.info("[INVENTORY-CONTROLLER] getInventoryControlPageData() with page {} and size {}", page, size);

        InventoryPageResponse inventoryPageResponse = inventoryService.getInventoryPageData(page, size);

        return ResponseEntity.ok(inventoryPageResponse);
    }

    /**
     * Search Pending Orders (Sales + Purchase)
     * Searches by: customer name, supplier name, order reference, PO number
     *
     * @param text search text
     * @param page page number
     * @param size page size
     * @return PaginatedResponse<PendingOrderResponse>
     */
    @GetMapping("/pending-orders/search")
    public ResponseEntity<PaginatedResponse<PendingOrderResponse>> searchPendingOrders(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[INVENTORY-CONTROLLER] Search pending orders with text: '{}' by user: {}", text, userId);

        PaginatedResponse<PendingOrderResponse> pendingOrders =
                inventoryService.searchPendingOrders(text, page, size);

        return ResponseEntity.ok(pendingOrders);
    }

    /**
     * Filter Pending Orders
     * Accepts String parameters (will be parsed internally)
     * Filter by:
     * - type: "SALES_ORDER" or "PURCHASE_ORDER"
     * - status: SalesOrderStatus or PurchaseOrderStatus (as string)
     * - category: ProductCategories (as string, for PO only)
     * - dateOption: "orderDate" or "estimatedDate"
     * - startDate, endDate: date range
     *
     * @return PaginatedResponse<PendingOrderResponse>
     */
    @GetMapping("/pending-orders/filter")
    public ResponseEntity<PaginatedResponse<PendingOrderResponse>> filterPendingOrders(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dateOption,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[INVENTORY-CONTROLLER] Filter pending orders - Type: {}, Status: {}, Category: {} by user: {}",
                type, status, category, userId);

        // Parse and validate type
        if (type != null && !type.equalsIgnoreCase("SALES_ORDER") && !type.equalsIgnoreCase("PURCHASE_ORDER")) {
            log.warn("[INVENTORY-CONTROLLER] Invalid type value: {}", type);
            return ResponseEntity.badRequest().build();
        }

        // Determine which status type (SO or PO)
        String soStatus = null;
        String poStatus = null;

        if (status != null) {
            // Try to determine if it's SO or PO status based on type or enum values
            if ("SALES_ORDER".equalsIgnoreCase(type)) {
                soStatus = status;
            } else if ("PURCHASE_ORDER".equalsIgnoreCase(type)) {
                poStatus = status;
            } else {
                // If type not specified, try both (service will handle parsing)
                soStatus = status;
                poStatus = status;
            }
        }

        String normalizedDateOption = GlobalServiceHelper.normalizeOptionDate(dateOption);

        // Delegate to service (service will parse enums internally)
        PaginatedResponse<PendingOrderResponse> result = inventoryService.filterPendingOrders(
                type, soStatus, poStatus, normalizedDateOption,
                startDate, endDate, category, sortBy, sortDirection, page, size
        );

        return ResponseEntity.ok(result);
    }
}
