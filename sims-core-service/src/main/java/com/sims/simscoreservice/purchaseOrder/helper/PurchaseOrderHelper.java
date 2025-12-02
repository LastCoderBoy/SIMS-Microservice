package com.sims.simscoreservice.purchaseOrder.helper;


import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.purchaseOrder.dto.SummaryPurchaseOrderView;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Purchase Order Helper
 * Utility methods for purchase orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Component
@RequiredArgsConstructor
public class PurchaseOrderHelper {

    /**
     * Convert entity to summary view
     */
    public SummaryPurchaseOrderView toSummaryView(PurchaseOrder order) {
        return new SummaryPurchaseOrderView(order);
    }

    /**
     * Convert Page to PaginatedResponse
     */
    public PaginatedResponse<SummaryPurchaseOrderView> toPaginatedSummaryView(Page<PurchaseOrder> purchaseOrderPage) {
        Page<SummaryPurchaseOrderView> viewPage = purchaseOrderPage.map(this::toSummaryView);
        return new PaginatedResponse<>(viewPage);
    }

    /**
     * Validate order ID
     */
    public void validateOrderId(Long orderId) {
        if (orderId == null) {
            throw new ValidationException("Purchase Order ID cannot be null");
        }
        if (orderId < 1) {
            throw new ValidationException("Purchase Order ID must be greater than zero");
        }
    }

    /**
     * Calculate total price
     */
    public static BigDecimal calculateTotalPrice(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getProduct().getPrice()
                .multiply(new BigDecimal(purchaseOrder. getOrderedQuantity()));
    }
}
