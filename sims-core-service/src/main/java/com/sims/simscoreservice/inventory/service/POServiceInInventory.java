package com.sims.simscoreservice.inventory.service;


import com.sims.common.models.ApiResponse;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.enums.ProductCategories;
import com.sims.simscoreservice.purchaseOrder.dto.ReceiveStockRequest;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import org.apache.coyote.BadRequestException;

import java.nio.file.AccessDeniedException;

/**
 * Purchase Order Service in Inventory
 * Manages pending purchase orders in inventory context
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface POServiceInInventory {

    /**
     * Get all pending purchase orders
     */
    PaginatedResponse<SummaryPurchaseOrderView> getAllPendingPurchaseOrders(int page, int size, String sortBy, String sortDirection);

    /**
     * Receive purchase order (Stock IN)
     */
    ApiResponse<Void> receivePurchaseOrder(Long orderId, ReceiveStockRequest receiveRequest, String username) throws BadRequestException;

    /**
     * Cancel purchase order
     */
    ApiResponse<Void> cancelPurchaseOrder(Long orderId, String username) throws BadRequestException, AccessDeniedException;

    /**
     * Search pending purchase orders
     */
    PaginatedResponse<SummaryPurchaseOrderView> searchPendingPurchaseOrders(String text, int page, int size, String sortBy, String sortDirection);

    /**
     * Filter pending purchase orders
     */
    PaginatedResponse<SummaryPurchaseOrderView> filterPendingPurchaseOrders(PurchaseOrderStatus status, ProductCategories category,
                                                                            String sortBy, String sortDirection, int page, int size);

    /**
     * Get all overdue purchase orders
     */
    PaginatedResponse<SummaryPurchaseOrderView> getAllOverduePurchaseOrders(int page, int size);
}
