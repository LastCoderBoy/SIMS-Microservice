package com.sims.simscoreservice.email.confirmationToken.service;

import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.email.confirmationToken.entity.ConfirmationToken;
import com.sims.simscoreservice.email.confirmationToken.enums.ConfirmationTokenStatus;
import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.enums.InventoryStatus;
import com.sims.simscoreservice.inventory.queryService.InventoryQueryService;
import com.sims.simscoreservice.inventory.service.InventoryService;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.product.services.ProductService;
import com.sims.simscoreservice.purchaseOrder.dto.PurchaseOrderDetailsView;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Purchase Order Confirmation Service
 * Handles supplier confirmation/cancellation via email
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderConfirmationService {

    private final ConfirmationTokenService confirmationTokenService;
    private final InventoryQueryService inventoryQueryService;
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Confirm purchase order from supplier email
     */
    @Transactional
    public ApiResponse<String> confirmPurchaseOrder(String token, LocalDate expectedArrivalDate) {
        // Validate token
        ConfirmationToken confirmationToken = confirmationTokenService.validateConfirmationToken(token);
        if (confirmationToken == null) {
            return ApiResponse.error("Email link is expired or already processed.");
        }

        PurchaseOrder order = confirmationToken.getOrder();

        // Check order status
        if (order.getStatus() != PurchaseOrderStatus.AWAITING_APPROVAL) {
            log.warn("[PO-CONFIRMATION] Order {} is not awaiting approval. Current status: {}",
                    order.getPoNumber(), order.getStatus());
            return ApiResponse.error("Order already confirmed or cancelled.");
        }

        try {
            // Update expected arrival date from supplier
            order.setExpectedArrivalDate(expectedArrivalDate);

            // Update inventory status if needed
            handleInventoryStatusUpdates(order.getProduct());

            // Set PO status to DELIVERY_IN_PROCESS
            order.setStatus(PurchaseOrderStatus.DELIVERY_IN_PROCESS);
            order.setUpdatedBy("Supplier via Confirmation Link");
            purchaseOrderRepository.save(order);

            // Update token status
            confirmationTokenService.updateConfirmationToken(confirmationToken, ConfirmationTokenStatus.CONFIRMED);

            log.info("[PO-CONFIRMATION] Order {} confirmed by supplier with arrival date: {}",
                    order.getPoNumber(), expectedArrivalDate);

            return ApiResponse.success("Order " + order.getPoNumber() + " confirmed successfully with expected arrival: " + expectedArrivalDate);

        } catch (OptimisticLockingFailureException | OptimisticLockException e) {
            log.warn("[PO-CONFIRMATION] Race condition detected while confirming the order: {}", order.getId());
            return ApiResponse.error("This order has already been processed by someone else.");
        } catch (Exception e) {
            log.error("[PO-CONFIRMATION] Error confirming order: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to confirm order.Please contact support.");
        }
    }

    /**
     * Cancel purchase order from supplier email
     */
    @Transactional
    public ApiResponse<String> cancelPurchaseOrder(String token) {
        // Validate token
        ConfirmationToken confirmationToken = confirmationTokenService.validateConfirmationToken(token);
        if (confirmationToken == null) {
            return ApiResponse.error("Email link is expired or already processed.");
        }

        PurchaseOrder order = confirmationToken.getOrder();

        // Check order status
        if (order.getStatus() != PurchaseOrderStatus.AWAITING_APPROVAL) {
            log.warn("[PO-CONFIRMATION] Order {} is not awaiting approval.Current status: {}",
                    order.getPoNumber(), order.getStatus());
            return ApiResponse.error("Order already confirmed or cancelled.");
        }

        try {
            // Set PO status to FAILED
            order.setStatus(PurchaseOrderStatus.FAILED);
            order.setUpdatedBy("Supplier via Email Link");
            purchaseOrderRepository.save(order);

            // Update token status
            confirmationTokenService.updateConfirmationToken(confirmationToken, ConfirmationTokenStatus.CANCELLED);

            log.info("[PO-CONFIRMATION] Order {} cancelled by supplier", order.getPoNumber());

            return ApiResponse.success("Order " + order.getPoNumber() + " has been successfully cancelled!");

        } catch (OptimisticLockingFailureException | OptimisticLockException e) {
            log.warn("[PO-CONFIRMATION] Race condition detected while cancelling the order: {}", order.getId());
            return ApiResponse.error("This order has already been processed by someone else.");
        } catch (Exception e) {
            log.error("[PO-CONFIRMATION] Error cancelling order: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to cancel order.Please contact support.");
        }
    }

    /**
     * Handle inventory status updates based on product status
     */
    private void handleInventoryStatusUpdates(Product orderedProduct) {
        Optional<Inventory> inventoryOpt = inventoryQueryService.getInventoryByProductId(orderedProduct.getProductId());

        if (orderedProduct.getStatus() == ProductStatus.PLANNING) {
            handlePlanningStatusUpdate(orderedProduct, inventoryOpt);
        } else if (orderedProduct.getStatus() == ProductStatus.ACTIVE) {
            handleActiveStatusUpdate(inventoryOpt);
        }
    }

    /**
     * If the Product is on PLANNING -> ON_ORDER
     * in the Inventory, set the status to INCOMING
     */
    private void handlePlanningStatusUpdate(Product orderedProduct, Optional<Inventory> inventoryOpt) {
        // Update product status from PLANNING to ON_ORDER
        orderedProduct.setStatus(ProductStatus.ON_ORDER);
        productService.saveProduct(orderedProduct);

        if (inventoryOpt.isEmpty()) {
            // Product not in inventory, add it with INCOMING status
            inventoryService.addProduct(orderedProduct, true);
        } else {
            // Update existing inventory status to INCOMING
            handleActiveStatusUpdate(inventoryOpt);
        }
    }

    /**
     * Product in the IC set to INCOMING
     */
    private void handleActiveStatusUpdate(Optional<Inventory> inventoryOpt) {
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            if (inventory.getStatus() != InventoryStatus.INCOMING) {
                inventoryService.updateInventoryStatus(inventoryOpt, InventoryStatus.INCOMING);
            }
        }
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDetailsView getPurchaseOrderDetailsByToken(String token) {
        // Validate token
        ConfirmationToken confirmationToken = confirmationTokenService.validateConfirmationToken(token);

        if (confirmationToken == null) {
            throw new ResourceNotFoundException("Invalid or expired token");
        }

        PurchaseOrder order = confirmationToken.getOrder();

        return new PurchaseOrderDetailsView(order);
    }
}
