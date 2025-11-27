package com.sims.simscoreservice.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inventory Metrics
 * Summary statistics for dashboard
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryMetrics {
    private Long totalCount;
    private Long lowStockCount;
}
