package com.sims.simscoreservice.inventory.controller;


import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.service.SOServiceInInventory;
import com.sims.simscoreservice.salesOrder.dto.ProcessSalesOrderRequestDto;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.sims.common.constants.AppConstants.*;

/**
 * Sales Order Controller in Inventory
 * Manages pending sales orders in inventory context
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_INVENTORY_PATH + "/sales-orders")
@RequiredArgsConstructor
@Slf4j
public class SOControllerInInventory {

    private final SOServiceInInventory soServiceInInventory;
    private final RoleValidator roleValidator;

    /**
     * Get all waiting/pending sales orders
     * Returns orders with status: PENDING, PARTIALLY_APPROVED, PARTIALLY_DELIVERED
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<SummarySalesOrderView>> getAllWaitingSalesOrders(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDir,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[SO-INVENTORY-CONTROLLER] Get all outgoing orders by user: {} - page: {}, size: {}, sortBy: {}, sortDir: {}",
                userId, page, size, sortBy, sortDir);

        PaginatedResponse<SummarySalesOrderView> orders =
                soServiceInInventory.getAllOutgoingSalesOrders(page, size, sortBy, sortDir);

        return ResponseEntity.ok(orders);
    }

    /**
     * Get all urgent sales orders (delivery < 2 days)
     */
    @GetMapping("/urgent")
    public ResponseEntity<PaginatedResponse<SummarySalesOrderView>> getAllUrgentSalesOrders(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) @Min(0) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "orderReference") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDir,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[SO-INVENTORY-CONTROLLER] Get urgent orders by user: {}", userId);

        PaginatedResponse<SummarySalesOrderView> urgentOrders =
                soServiceInInventory.getAllUrgentSalesOrders(page, size, sortBy, sortDir);

        return ResponseEntity.ok(urgentOrders);
    }

    /**
     * STOCK OUT - Process Sales Order (Bulk)
     * Fulfills sales order and deducts stock from inventory
     * Only ADMIN/MANAGER can process
     */
    @PutMapping("/stocks/out")
    public ResponseEntity<ApiResponse<Void>> bulkStockOutOrders(
            @Valid @RequestBody ProcessSalesOrderRequestDto request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SO-INVENTORY-CONTROLLER] Bulk stock out for order {} with {} items by user: {}",
                request.getOrderId(), request.getItemQuantities().size(), userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        ApiResponse<Void> response = soServiceInInventory.processSalesOrder(request, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * CANCEL - Cancel Sales Order
     * Releases reserved stock when order is cancelled
     * Only ADMIN/MANAGER can cancel
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSalesOrder(
            @PathVariable Long orderId,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SO-INVENTORY-CONTROLLER] Cancel order {} by user: {}", orderId, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        ApiResponse<Void> apiResponse = soServiceInInventory.cancelSalesOrder(orderId, userId);

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Search pending sales orders
     * Searches by: Order Reference, Customer Name
     */
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<SummarySalesOrderView>> searchInWaitingSalesOrders(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "orderReference") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDir,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[SO-INVENTORY-CONTROLLER] Search pending orders with text '{}' by user: {}", text, userId);

        PaginatedResponse<SummarySalesOrderView> dtoResponse =
                soServiceInInventory.searchInWaitingSalesOrders(text, page, size, sortBy, sortDir);

        return ResponseEntity.ok(dtoResponse);
    }

    /**
     * Filter pending sales orders
     * Uses SalesOrderStatus enum with converter
     * Filter by:
     * - status: SalesOrderStatus enum
     * - optionDate: orderDate, deliveryDate, estimatedDeliveryDate
     * - startDate, endDate: date range
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<SummarySalesOrderView>> filterWaitingSalesOrders(
            @RequestParam(required = false) SalesOrderStatus status,
            @RequestParam(required = false) String optionDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[SO-INVENTORY-CONTROLLER] Filter pending orders - status: {}, optionDate: {} by user: {}",
                status, optionDate, userId);

        try {
            // Validate and normalize optionDate
            String optionDateValue = GlobalServiceHelper.normalizeOptionDate(optionDate);

            PaginatedResponse<SummarySalesOrderView> dtoResponse =
                    soServiceInInventory.filterWaitingSoProducts(status, optionDateValue, startDate, endDate,
                            page, size, sortBy, sortDirection);

            return ResponseEntity.ok(dtoResponse);

        } catch (IllegalArgumentException e) {
            log.error("[SO-INVENTORY-CONTROLLER] Invalid filter parameters: {}", e.getMessage());
            throw new ValidationException("Invalid filter parameters: " + e.getMessage());
        } catch (Exception e) {
            log.error("[SO-INVENTORY-CONTROLLER] Error filtering orders: {}", e.getMessage());
            throw new ServiceException("Failed to filter orders", e);
        }
    }
}