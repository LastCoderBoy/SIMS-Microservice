package com.sims.simscoreservice.orderManagement.controller;

import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.orderManagement.service.SalesOrderService;
import com.sims.simscoreservice.salesOrder.dto.DetailedSalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.SalesOrderRequest;
import com.sims.simscoreservice.salesOrder.dto.SummarySalesOrderView;
import com.sims.simscoreservice.salesOrder.dto.orderItem.BulkOrderItemsRequestDto;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.sims.common.constants.AppConstants.*;

/**
 * Sales Order Controller in the Order Management context
 * Handles sales order CRUD operations
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_ORDER_MANAGEMENT_PATH + "/sales-orders")
@RequiredArgsConstructor
@Slf4j
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final RoleValidator roleValidator;

    @GetMapping
    public ResponseEntity<PaginatedResponse<SummarySalesOrderView>> getAllSalesOrders(
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_SO) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {

        log.info("[SO-CONTROLLER] Getting all sales orders (page={}, size={})", page, size);

        PaginatedResponse<SummarySalesOrderView> response =
                salesOrderService.getAllSummarySalesOrders(sortBy, sortDirection, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<DetailedSalesOrderView>> getSalesOrderDetails(@PathVariable Long orderId) {
        log.info("[SO-CONTROLLER] Getting details for sales order: {}", orderId);

        DetailedSalesOrderView detailedView = salesOrderService.getDetailsForSalesOrderId(orderId);

        return ResponseEntity.ok(ApiResponse.success("Sales order retrieved successfully", detailedView));
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<SummarySalesOrderView>> searchSalesOrders(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_SO) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection) {

        log.info("[SO-CONTROLLER] Searching sales orders with text:  '{}'", text);

        PaginatedResponse<SummarySalesOrderView> response =
                salesOrderService.searchInSalesOrders(text, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<SummarySalesOrderView>> filterSalesOrders(
            @RequestParam(required = false) SalesOrderStatus status,
            @RequestParam(required = false) String optionDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_SO) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection) {

        log.info("[SO-CONTROLLER] Filtering sales orders (status={}, dateOption={})", status, optionDate);

        try {
            // Validate and normalize optionDate
            String optionDateValue = GlobalServiceHelper.normalizeOptionDate(optionDate);

            PaginatedResponse<SummarySalesOrderView> response =
                    salesOrderService.filterSalesOrders(status, optionDateValue, startDate, endDate,
                            page, size, sortBy, sortDirection);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid filter parameters: " + e.getMessage());
        }
    }

    // ========================================
    // WRITE OPERATIONS
    // ========================================

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createSalesOrder(
            @Valid @RequestBody SalesOrderRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SO-CONTROLLER] Creating sales order for user: {}", userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER"); // might throw Forbidden Exception

        ApiResponse<String> response = salesOrderService.createSalesOrder(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<String>> updateSalesOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody SalesOrderRequest request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SO-CONTROLLER] Updating sales order {} by user: {}", orderId, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER"); // might throw Forbidden Exception

        ApiResponse<String> response = salesOrderService.updateSalesOrder(orderId, request, userId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<String>> addItemsToSalesOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody BulkOrderItemsRequestDto request,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SO-CONTROLLER] Adding items to sales order {} by user:  {}", orderId, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER"); // might throw Forbidden Exception

        ApiResponse<String> response = salesOrderService.addItemsToSalesOrder(orderId, request, userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<ApiResponse<String>> removeItemFromSalesOrder(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        log.info("[SO-CONTROLLER] Removing item {} from order {} by user: {}", itemId, orderId, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER"); // might throw Forbidden Exception

        ApiResponse<String> response = salesOrderService.removeItemFromSalesOrder(orderId, itemId, userId);

        return ResponseEntity.ok(response);
    }
}
