package com.sims.simscoreservice.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Purchase Order Summary
 * Breakdown of purchase orders by status
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderSummary {

    private Long totalAwaitingApproval;
    private Long totalDeliveryInProcess;
    private Long totalPartiallyReceived;
    private Long totalReceived;
    private Long totalCancelled;
    private Long totalFailed;

    /**
     * Get total valid purchase orders
     */
    public Long getTotalValid() {
        return totalAwaitingApproval + totalDeliveryInProcess + totalPartiallyReceived;
    }

    /**
     * Get total orders (all statuses)
     */
    public Long getTotalOrders() {
        return totalAwaitingApproval + totalDeliveryInProcess + totalPartiallyReceived
                + totalReceived + totalCancelled + totalFailed;
    }

    /**
     * Calculate success rate percentage
     */
    public Double getSuccessRate() {
        Long total = getTotalOrders();
        return total > 0 ? (totalReceived * 100.0) / total : 0.0;
    }
}
