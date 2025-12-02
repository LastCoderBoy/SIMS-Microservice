package com.sims.simscoreservice.inventory.controller;

import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.inventory.service.POServiceInInventory;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.ReceiveStockRequest;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

import static com.sims.common.constants.AppConstants.*;

/**
 * Purchase Order Controller in Inventory
 * Manages pending purchase orders in inventory context
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_INVENTORY_PATH + "/purchase-orders")
@RequiredArgsConstructor
@Slf4j
public class POControllerInInventory {

    private final POServiceInInventory poServiceInInventory;
    private final RoleValidator roleValidator;

    /**
     * Get all pending purchase orders
     * Returns orders with status: AWAITING_APPROVAL, DELIVERY_IN_PROCESS, PARTIALLY_RECEIVED
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<SummaryPurchaseOrderView>> getAllPendingPurchaseOrders(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_PO) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PO-INVENTORY-CONTROLLER] Get all pending POs by user: {}", userId);

        PaginatedResponse<SummaryPurchaseOrderView> response =
                poServiceInInventory.getAllPendingPurchaseOrders(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all overdue purchase orders
     * Returns orders where expectedArrivalDate < today AND not finalized
     */
    @GetMapping("/overdue")
    public ResponseEntity<PaginatedResponse<SummaryPurchaseOrderView>> getAllOverduePurchaseOrders(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PO-INVENTORY-CONTROLLER] Get overdue POs by user: {}", userId);

        PaginatedResponse<SummaryPurchaseOrderView> response =
                poServiceInInventory.getAllOverduePurchaseOrders(page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * Search pending purchase orders
     * Searches by: PO Number, Supplier Name, Product Name
     */
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<SummaryPurchaseOrderView>> searchPendingPurchaseOrders(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_PO) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PO-INVENTORY-CONTROLLER] Search POs with text '{}' by user: {}", text, userId);

        PaginatedResponse<SummaryPurchaseOrderView> response =
                poServiceInInventory.searchPendingPurchaseOrders(text, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    /**
     * Filter pending purchase orders
     * Uses enum parameters with converters
     *
     * @param status PurchaseOrderStatus enum
     * @param category ProductCategories enum
     */
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<SummaryPurchaseOrderView>> filterPendingPurchaseOrders(
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) ProductCategories category,
            @RequestParam(defaultValue = DEFAULT_SORT_BY_FOR_PO) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestHeader(USER_ID_HEADER) String userId) {

        log.info("[PO-INVENTORY-CONTROLLER] Filter POs by status: {}, category: {} by user: {}",
                status, category, userId);

        PaginatedResponse<SummaryPurchaseOrderView> response =
                poServiceInInventory.filterPendingPurchaseOrders(status, category, sortBy, sortDirection, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * STOCK IN - Receive Purchase Order
     * Updates inventory when order arrives
     * Only ADMIN/MANAGER can receive
     */
    @PutMapping("/{orderId}/receive")
    public ResponseEntity<ApiResponse<Void>> receivePurchaseOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody ReceiveStockRequest receiveRequest,
            @RequestHeader("Authorization") String token,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) throws BadRequestException {

        log.info("[PO-INVENTORY-CONTROLLER] Receive PO {} by user: {}", orderId, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        ApiResponse<Void> response = poServiceInInventory.receivePurchaseOrder(orderId, receiveRequest, userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * CANCEL - Cancel Purchase Order
     * Cancels pending order and updates product/inventory status
     * Only ADMIN/MANAGER can cancel
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPurchaseOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles) throws BadRequestException, AccessDeniedException {

        log.info("[PO-INVENTORY-CONTROLLER] Cancel PO {} by user: {}", orderId, userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        // Extract username from token
        String jwtToken = GlobalServiceHelper.validateAndExtractToken(token);

        ApiResponse<Void> response = poServiceInInventory.cancelPurchaseOrder(orderId, userId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
