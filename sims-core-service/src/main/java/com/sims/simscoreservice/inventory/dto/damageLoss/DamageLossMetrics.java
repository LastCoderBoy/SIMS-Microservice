package com.sims.simscoreservice.inventory.dto.damageLoss;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Damage/Loss Metrics DTO
 * Summary statistics for dashboard
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DamageLossMetrics {
    private Long totalReports;
    private Long totalItemsLost;
    private BigDecimal totalLossValue;
}
