package com.sims.simscoreservice.inventory.service.impl;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.analytics.dto.PurchaseOrderSummary;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.queryService.InventoryQueryService;
import com.sims.simscoreservice.inventory.service.InventoryStatusService;
import com.sims.simscoreservice.inventory.service.POServiceInInventory;
import com.sims.simscoreservice.stockManagement.StockManagementService;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.product.helper.ProductStatusModifier;
import com.sims.simscoreservice.purchaseOrder.dto.ReceiveStockRequest;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.purchaseOrder.queryService.PurchaseOrderQueryService;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import com.sims.simscoreservice.purchaseOrder.strategy.PurchaseOrderSearchService;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.stockMovement.enums.StockMovementReferenceType;
import com.sims.simscoreservice.stockMovement.enums.StockMovementType;
import com.sims.simscoreservice.stockMovement.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Purchase Order Service in Inventory Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class POServiceInInventoryImpl implements POServiceInInventory {

    // ========== Components ==========
    private final Clock clock;
    private final ProductStatusModifier productStatusModifier;

    // ========== Services ==========
    private final PurchaseOrderQueryService purchaseOrderQueryService;
    private final PurchaseOrderSearchService purchaseOrderSearchService;
    private final InventoryQueryService inventoryQueryService;
    private final StockManagementService stockManagementService;
    private final InventoryStatusService inventoryStatusService;
    private final StockMovementService stockMovementService; // log the stock movements

    // ========== Repositories ==========
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> getAllPendingPurchaseOrders(int page, int size, String sortBy, String sortDirection) {
        return purchaseOrderQueryService.getAllPendingPurchaseOrders(page, size, sortBy, sortDirection);
    }

    @Override
    @Transactional
    public ApiResponse<Void> receivePurchaseOrder(Long orderId, @Valid ReceiveStockRequest receiveRequest,
                                                  String username) throws BadRequestException {
        try {
            // Validate order ID
            if (orderId == null || orderId < 1) {
                throw new IllegalArgumentException("Invalid order ID: " + orderId);
            }

            // Find purchase order
            PurchaseOrder order = purchaseOrderQueryService.findById(orderId);

            // Check if order can be received
            if (order.isFinalized()) {
                throw new ValidationException("Cannot receive stock for finalized order with status: " + order.getStatus());
            }

            // Update order with received stock
            updateOrderWithReceivedStock(order, receiveRequest);

            // Update inventory levels
            updateInventoryLevels(order, receiveRequest.getReceivedQuantity());

            // Save order
            order.setUpdatedBy(username);
            purchaseOrderRepository.save(order);
            purchaseOrderRepository.flush();  // Populate timestamps

             stockMovementService.logMovement(order.getProduct(), StockMovementType.IN,
                 receiveRequest.getReceivedQuantity(), order.getPoNumber(),
                 StockMovementReferenceType.PURCHASE_ORDER, username);

            log.info("[PO-INVENTORY] Purchase order {} received successfully by {}", order.getPoNumber(), username);

            return ApiResponse.success("Purchase order received successfully");

        } catch (IllegalArgumentException | ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PO-INVENTORY] Error receiving purchase order: {}", e.getMessage());
            throw new ServiceException("Failed to receive purchase order", e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<Void> cancelPurchaseOrder(Long orderId, String username){
        try {
            // Validate order ID
            if (orderId == null || orderId < 1) {
                throw new IllegalArgumentException("Invalid order ID: " + orderId);
            }

            // Find purchase order
            PurchaseOrder order = purchaseOrderQueryService.findById(orderId);

            // Check if order can be cancelled
            if (order.isFinalized()) {
                throw new ValidationException("Cannot cancel order with status: " + order.getStatus());
            }

            // Update order status
            order.setStatus(PurchaseOrderStatus.CANCELLED);
            order.setUpdatedBy(username);

            // Update product status in PM
            productStatusModifier.updateIncomingProductStatusInPm(order.getProduct());

            // Update inventory status
            Optional<Inventory> inventoryOpt = inventoryQueryService.getInventoryByProductId(
                    order.getProduct().getProductId()
            );
            inventoryOpt.ifPresent(inventoryStatusService::updateInventoryStatus);

            // Save order
            purchaseOrderRepository.save(order);

            log.info("[PO-INVENTORY] Purchase order {} cancelled by {}", order.getPoNumber(), username);

            return ApiResponse.success("Purchase order cancelled successfully");

        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[PO-INVENTORY] Database error cancelling order: {}", e.getMessage());
            throw new DatabaseException("Failed to cancel purchase order", e);
        } catch (Exception e) {
            log.error("[PO-INVENTORY] Error cancelling order: {}", e.getMessage());
            throw new ServiceException("Failed to cancel purchase order", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> searchPendingPurchaseOrders(String text, int page, int size,
                                                                                   String sortBy, String sortDirection) {
        return purchaseOrderSearchService.searchPending(text, page, size, sortBy, sortDirection);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> filterPendingPurchaseOrders(PurchaseOrderStatus status,
                                                                                   ProductCategories category,
                                                                                   String sortBy, String sortDirection,
                                                                                   int page, int size) {
        return purchaseOrderSearchService.filterPending(status, category, sortBy, sortDirection, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SummaryPurchaseOrderView> getAllOverduePurchaseOrders(int page, int size) {
        return purchaseOrderQueryService.getAllOverduePurchaseOrders(page, size);
    }

    /**
     * Update purchase order with received stock
     */
    private void updateOrderWithReceivedStock(PurchaseOrder order, ReceiveStockRequest receiveRequest) {
        // Set actual arrival date
        if (receiveRequest.getActualArrivalDate() != null) {
            if (receiveRequest.getActualArrivalDate().isAfter(LocalDate.now())) {
                throw new ValidationException("Actual arrival date cannot be in the future");
            }
            order.setActualArrivalDate(receiveRequest.getActualArrivalDate());
        } else if (order.getActualArrivalDate() == null) {
            order.setActualArrivalDate(GlobalServiceHelper.now(clock).toLocalDate());
        }

        // Update received quantity
        int receivedQuantity = receiveRequest.getReceivedQuantity();
        if (receivedQuantity > order.getOrderedQuantity()) {
            throw new ValidationException("Cannot receive more than ordered quantity");
        }
        order.setReceivedQuantity(order.getReceivedQuantity() + receivedQuantity);

        // Update status based on received quantity
        updateOrderStatus(order);
    }

    /**
     * Update order status based on received quantity
     */
    private void updateOrderStatus(PurchaseOrder order) {
        if (order.getReceivedQuantity() >= order.getOrderedQuantity()) {
            order.setStatus(PurchaseOrderStatus.RECEIVED);
            productStatusModifier.updateIncomingProductStatusInPm(order.getProduct());
        } else if (order.getReceivedQuantity() > 0) {
            order.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
    }

    /**
     * Update inventory stock levels
     */
    private void updateInventoryLevels(PurchaseOrder order, int receivedQuantity) {
        try {
            Optional<Inventory> inventoryOpt = inventoryQueryService.getInventoryByProductId(
                    order.getProduct().getProductId()
            );

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();
                int newStockLevel = inventory.getCurrentStock() + receivedQuantity;

                // Update stock levels
                stockManagementService.updateStockLevels(inventory, newStockLevel, null);

                log.info("[PO-INVENTORY] Updated inventory {} stock: {} -> {}",
                        inventory.getSku(), inventory.getCurrentStock(), newStockLevel);
            }
        } catch (Exception e) {
            log.error("[PO-INVENTORY] Failed to update inventory: {}", e.getMessage());
            throw new ServiceException("Failed to update inventory levels", e);
        }
    }
}
