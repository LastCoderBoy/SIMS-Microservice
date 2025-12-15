package com.sims.simscoreservice.inventory.dto.lowStock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Low Stock Metrics
 * Summary statistics for Low Stock dashboard
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LowStockMetrics {
    private long totalLowStockItems;
    private long criticalLowStockItems;
    private double averageLowStockLevel;
}
