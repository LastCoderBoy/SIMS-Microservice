package com.sims.simscoreservice.salesOrder.processor;

import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.exceptions.InsufficientStockException;
import com.sims.simscoreservice.exceptions.InventoryException;
import com.sims.simscoreservice.inventory.stockManagement.StockManagementService;
import com.sims.simscoreservice.salesOrder.entity.OrderItem;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.OrderItemStatus;
import com.sims.simscoreservice.salesOrder.helper.SalesOrderHelper;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.stockMovement.enums.StockMovementReferenceType;
import com.sims.simscoreservice.stockMovement.enums.StockMovementType;
import com.sims.simscoreservice.stockMovement.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;

/**
 * Abstract Order Processor
 * Base class for order processing logic
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Slf4j
@RequiredArgsConstructor
public abstract class OrderProcessor {

    protected final Clock clock;
    protected final SalesOrderHelper salesOrderHelper;
    protected final StockManagementService stockManagementService;
    protected final StockMovementService stockMovementService;

    /**
     * Process order fulfillment
     *
     * @param salesOrder Sales order to process
     * @param approvedQuantities Map of productId -> approved quantity
     * @param approvedPerson User approving the order
     * @return Updated sales order
     */
    @Transactional
    public SalesOrder processOrder(SalesOrder salesOrder, Map<String, Integer> approvedQuantities, String approvedPerson) {
        log.info("[ORDER-PROCESSOR] Processing order: {}", salesOrder.getOrderReference());

        try {
            // Check if order is finalized
            if (salesOrder.isFinalized()) {
                throw new ResourceNotFoundException("Order is finalized, cannot process: " + salesOrder.getOrderReference());
            }

            // Set approval metadata
            salesOrder.setConfirmedBy(approvedPerson);
            salesOrder.setLastUpdate(GlobalServiceHelper.now(clock));

            // Process each order item
            for (OrderItem item : salesOrder.getItems()) {
                String productId = item.getProduct().getProductId();
                Integer approvedQty = approvedQuantities.get(productId);

                // Skip if no approved quantity provided
                if (approvedQty == null) {
                    log.warn("[ORDER-PROCESSOR] Skipping item {} - no approved quantity", productId);
                    continue;
                }

                // Validate approved quantity
                validateApprovedQuantity(item, approvedQty, productId);

                // Fulfill reservation (deduct from inventory)
                stockManagementService.fulfillReservation(productId, approvedQty);

                // Update order item status
                updateOrderItemFulfillStatus(item, approvedQty);

                // Update approved quantity
                item.setApprovedQuantity(item.getApprovedQuantity() + approvedQty);

                 stockMovementService.logMovement(
                     item.getProduct(), StockMovementType.OUT, approvedQty,
                     salesOrder.getOrderReference(), StockMovementReferenceType.SALES_ORDER, approvedPerson
                 );

                log.info("[ORDER-PROCESSOR] Processed item {} - approved: {}", productId, approvedQty);
            }

            // Update overall order status based on items
            salesOrderHelper.updateSoStatusBasedOnItemQuantity(salesOrder);

            log.info("[ORDER-PROCESSOR] Order {} processed successfully", salesOrder.getOrderReference());

            return salesOrder;

        } catch (InsufficientStockException e) {
            log.error("[ORDER-PROCESSOR] Insufficient stock: {}", e.getMessage());
            throw e;
        } catch (InventoryException e) {
            log.error("[ORDER-PROCESSOR] Inventory error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ORDER-PROCESSOR] Error processing order: {}", e.getMessage());
            throw new ServiceException("Failed to process order", e);
        }
    }

    /**
     * Validate approved quantity
     */
    private void validateApprovedQuantity(OrderItem item, Integer approvedQty, String productId) {
        if (approvedQty < 0) {
            log.error("[ORDER-PROCESSOR] Negative approved quantity for: {}", productId);
            throw new InventoryException("Cannot approve negative stock for item: " + productId);
        }

        if (approvedQty > item.getQuantity()) {
            log.error("[ORDER-PROCESSOR] Approved quantity exceeds ordered quantity for: {}", productId);
            throw new InventoryException("Cannot approve more than ordered quantity for item: " + productId);
        }
    }

    /**
     * Update order item fulfillment status
     */
    private void updateOrderItemFulfillStatus(OrderItem orderItem, int approvedQuantity) {
        if (approvedQuantity < orderItem.getQuantity()) {
            orderItem.setStatus(OrderItemStatus.PARTIALLY_APPROVED);
            log.info("[ORDER-PROCESSOR] Item {} set to PARTIALLY_APPROVED", orderItem.getId());
        } else {
            orderItem.setStatus(OrderItemStatus.APPROVED);
            log.info("[ORDER-PROCESSOR] Item {} set to APPROVED", orderItem.getId());
        }
    }
}
