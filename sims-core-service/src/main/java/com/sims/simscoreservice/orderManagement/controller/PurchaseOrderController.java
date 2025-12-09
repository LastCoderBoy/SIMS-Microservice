package com.sims.simscoreservice.orderManagement.controller;

import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.orderManagement.service.PurchaseOrderService;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.PurchaseOrderDetailsView;
import com.sims.simscoreservice.purchaseOrder.dto.PurchaseOrderRequest;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sims.common.constants.AppConstants.*;

/**
 * Purchase Order Controller (Order Management)
 * Handles PO creation, viewing, and searching
 * Stock operations (receive/cancel) are in POControllerInInventory
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_ORDER_MANAGEMENT_PATH + "/purchase-orders")
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final RoleValidator roleValidator;

    /**
     * Get all purchase orders with pagination
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<SummaryPurchaseOrderView>> getAllPurchaseOrders(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_PO) String sortBy,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PO-OM-CONTROLLER] Get all POs by user: {}", userId);

        PaginatedResponse<SummaryPurchaseOrderView> pageResponse =
                purchaseOrderService.getAllPurchaseOrders(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(pageResponse);
    }

    /**
     * Get purchase order details
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<PurchaseOrderDetailsView> getPurchaseOrderDetails(
            @PathVariable Long orderId,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PO-OM-CONTROLLER] Get PO details for ID: {} by user: {}", orderId, userId);

        PurchaseOrderDetailsView detailsForPurchaseOrder =
                purchaseOrderService.getDetailsForPurchaseOrder(orderId);

        return ResponseEntity.ok(detailsForPurchaseOrder);
    }

    /**
     * Create new purchase order
     * Only ADMIN/MANAGER can create
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderRequest>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest stockRequest,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) throws BadRequestException {

        log.info("[PO-OM-CONTROLLER] Create PO by user: {}", userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER"); // might throw Forbidden Exception

        ApiResponse<PurchaseOrderRequest> response =
                purchaseOrderService.createPurchaseOrder(stockRequest, userId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Search purchase orders
     */
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<SummaryPurchaseOrderView>> searchPurchaseOrders(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_PO) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PO-OM-CONTROLLER] Search POs with text: '{}' by user: {}", text, userId);

        PaginatedResponse<SummaryPurchaseOrderView> response =
                purchaseOrderService.searchPurchaseOrders(text, page, size, sortBy, sortDirection);

        return ResponseEntity. ok(response);
    }

    /**
     * Filter purchase orders
     * Uses enum parameters with converters
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<SummaryPurchaseOrderView>> filterPurchaseOrders(
            @RequestParam(required = false) ProductCategories category,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_PO) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PO-OM-CONTROLLER] Filter POs - category: {}, status: {} by user: {}",
                category, status, userId);

        PaginatedResponse<SummaryPurchaseOrderView> filterResponse =
                purchaseOrderService.filterPurchaseOrders(category, status, sortBy, sortDirection, page, size);

        return ResponseEntity.ok(filterResponse);
    }
}
