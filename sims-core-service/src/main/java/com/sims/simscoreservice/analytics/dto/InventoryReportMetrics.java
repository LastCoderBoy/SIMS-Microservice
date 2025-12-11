package com.sims.simscoreservice.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inventory Report Metrics
 * Detailed inventory health and stock metrics
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReportMetrics {

    private BigDecimal totalStockValueAtRetail;
    private Long totalStockQuantity;
    private Long totalReservedStock;
    private Long availableStock; // currentStock - reservedStock

    // Stock Health Breakdown
    private Long inStockItems;      // currentStock > minLevel
    private Long lowStockItems;     // currentStock <= minLevel AND > 0
    private Long outOfStockItems;   // currentStock = 0

    /**
     * Calculate stock utilization percentage
     */
    public Double getStockUtilization() {
        return totalStockQuantity > 0
                ? (totalReservedStock * 100.0) / totalStockQuantity
                : 0.0;
    }

    /**
     * Calculate inventory health score
     */
    public Double getHealthScore() {
        long totalItems = inStockItems + lowStockItems + outOfStockItems;
        if (totalItems == 0) return 100.0;

        double lowStockPenalty = (lowStockItems * 100.0 / totalItems) * 0.5;
        double outOfStockPenalty = (outOfStockItems * 100.0 / totalItems) * 1.0;

        return Math.max(0, 100.0 - lowStockPenalty - outOfStockPenalty);
    }

    /**
     * Get health status label
     */
    public String getHealthStatus() {
        double score = getHealthScore();
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "GOOD";
        if (score >= 60) return "FAIR";
        if (score >= 40) return "POOR";
        return "CRITICAL";
    }
}
