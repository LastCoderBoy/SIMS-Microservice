package com.sims.simscoreservice.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sales Order Summary
 * Breakdown of sales orders by status
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderSummary {

    private Long totalCompleted;
    private Long totalPending;
    private Long totalDeliveryInProcess;
    private Long totalDelivered;
    private Long totalApproved;
    private Long totalPartiallyApproved;
    private Long totalPartiallyDelivered;
    private Long totalCancelled;

    /**
     * Get total orders in progress
     */
    public Long getTotalInProgress() {
        return totalPending + totalDeliveryInProcess + totalApproved
                + totalPartiallyApproved + totalPartiallyDelivered;
    }

    /**
     * Get total orders (all statuses)
     */
    public Long getTotalOrders() {
        return totalPending + totalDeliveryInProcess + totalDelivered
                + totalApproved + totalPartiallyApproved + totalPartiallyDelivered
                + totalCancelled + + totalCompleted;
    }

    /**
     * Calculate completion rate percentage
     */
    public Double getCompletionRate() {
        Long total = getTotalOrders();
        return total > 0 ? (totalDelivered * 100.0) / total : 0.0;
    }
}
