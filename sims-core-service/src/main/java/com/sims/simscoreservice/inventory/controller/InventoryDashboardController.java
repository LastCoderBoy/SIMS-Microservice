package com.sims.simscoreservice.inventory.controller;

import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.dto.InventoryPageResponse;
import com.sims.simscoreservice.inventory.dto.PendingOrderResponse;
import com.sims.simscoreservice.inventory.dto.ProcessSalesOrderRequestDto;
import com.sims.simscoreservice.inventory.dto.ReceiveStockRequest;
import com.sims.simscoreservice.inventory.service.InventoryService;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
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
    private final RoleValidator roleValidator;

    // TODO: Inject when implementing PO/SO services
    // private final POServiceInInventory poServiceInInventory;
    // private final SOServiceInInventory soServiceInInventory;

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
     * STOCK IN - Receive Purchase Order
     * Updates inventory when PO is received from supplier
     *
     * @param orderId Purchase Order ID
     * @param receiveRequest Receive stock request (quantities, etc.)
     * @param token JWT token
     * @return ApiResponse<Void>
     */
    @PutMapping("/{orderId}/receive")
    public ResponseEntity<ApiResponse<Void>> receiveIncomingStockOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody ReceiveStockRequest receiveRequest,
            @RequestHeader("Authorization") String token) throws BadRequestException, AccessDeniedException {

        log.info("[INVENTORY-CONTROLLER] receiveIncomingStockOrder() for PO ID: {}", orderId);

        // Validate and extract token
        String jwtToken = GlobalServiceHelper.validateAndExtractToken(token);

        // TODO: Implement when PO service is ready
        // ApiResponse<Void> response = poServiceInInventory.receivePurchaseOrder(orderId, receiveRequest, jwtToken);

        ApiResponse<Void> response = ApiResponse. success("Purchase Order received successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * STOCK OUT - Process Sales Order (Bulk)
     * Fulfills sales order and deducts stock from inventory
     *
     * @param request ProcessSalesOrderRequestDto (contains order items and quantities)
     * @param token JWT token
     * @return ApiResponse<Void>
     */
    @PutMapping("/stocks/out")
    public ResponseEntity<ApiResponse<Void>> bulkStockOutOrders(
            @Valid @RequestBody ProcessSalesOrderRequestDto request,
            @RequestHeader("Authorization") String token) {

        log. info("[INVENTORY-CONTROLLER] bulkStockOutOrders() called with {} orders",
                request.getItemQuantities().size());

        // Validate and extract token
        String jwtToken = GlobalServiceHelper.validateAndExtractToken(token);

        // TODO: Implement when SO service is ready
        // ApiResponse<Void> response = soServiceInInventory.processSalesOrder(request, jwtToken);

        ApiResponse<Void> response = ApiResponse.success("Sales Orders processed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * CANCEL ORDER - Cancel Sales Order
     * Releases reserved stock when order is cancelled
     *
     * @param orderId Sales Order ID
     * @param token JWT token
     * @return ApiResponse<Void>
     */
    @PutMapping("/{orderId}/cancel-order")
    public ResponseEntity<ApiResponse<Void>> cancelOutgoingStockOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) throws AccessDeniedException {

        log.info("[INVENTORY-CONTROLLER] cancelOutgoingStockOrder() for SO ID: {}", orderId);

        // Validate and extract token
        String jwtToken = GlobalServiceHelper.validateAndExtractToken(token);

        // TODO: Implement when SO service is ready
        // ApiResponse<Void> response = soServiceInInventory.cancelSalesOrder(orderId, jwtToken);

        ApiResponse<Void> response = ApiResponse.success("Sales Order cancelled successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Search Pending Orders (Sales + Purchase)
     * Searches by customer name, supplier name, order reference, PO number
     *
     * @param text search text
     * @param page page number
     * @param size page size
     * @return PaginatedResponse<PendingOrderResponse>
     */
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<PendingOrderResponse>> searchPendingOrders(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("[INVENTORY-CONTROLLER] searchPendingOrders() with text: '{}'", text);

        PaginatedResponse<PendingOrderResponse> pendingOrders =
                inventoryService.searchPendingOrders(text, page, size);

        return ResponseEntity.ok(pendingOrders);
    }

    /**
     * Filter Pending Orders
     * Filter by:
     * - type: "SALES_ORDER" or "PURCHASE_ORDER"
     * - status: SalesOrderStatus or PurchaseOrderStatus
     * - category: ProductCategories (for PO)
     * - dateOption: "orderDate" or "estimatedDate"
     * - startDate, endDate: date range
     *
     * @return PaginatedResponse<PendingOrderResponse>
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<PendingOrderResponse>> filterPendingOrders(
            @RequestParam(required = false) String type, // "SALES_ORDER" or "PURCHASE_ORDER"
            @RequestParam(required = false) String status, // SalesOrderStatus or PurchaseOrderStatus
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dateOption, // "orderDate" or "estimatedDate"
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat. ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO. DATE) LocalDate endDate,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("[INVENTORY-CONTROLLER] filterPendingOrders() - Type: {}, Status: {}, Category: {}",
                type, status, category);

        // Parse status (handle both SO and PO statuses)
        String soStatus = null;
        String poStatus = null;

        if (status != null) {
            try {
                // Try parsing as SalesOrderStatus
                // SalesOrderStatus. valueOf(status.toUpperCase());
                soStatus = status;
            } catch (IllegalArgumentException e) {
                try {
                    // Try parsing as PurchaseOrderStatus
                    // PurchaseOrderStatus.valueOf(status.toUpperCase());
                    poStatus = status;
                } catch (IllegalArgumentException ex) {
                    log.warn("[INVENTORY-CONTROLLER] Invalid status value: {}", status);
                }
            }
        }

        // Parse category (if provided)
        ProductCategories productCategory = null;
        if (category != null) {
            try {
                productCategory = ProductCategories.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[INVENTORY-CONTROLLER] Invalid category value: {}", category);
            }
        }

        // Delegate to service
        PaginatedResponse<PendingOrderResponse> result = inventoryService.filterPendingOrders(
                type,
                soStatus,
                poStatus,
                dateOption,
                startDate,
                endDate,
                category,  // Pass as string, service will convert
                sortBy,
                sortDirection,
                page,
                size
        );

        return ResponseEntity.ok(result);
    }
}
