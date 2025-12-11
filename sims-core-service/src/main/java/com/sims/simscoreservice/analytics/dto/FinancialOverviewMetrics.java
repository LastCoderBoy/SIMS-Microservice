package com.sims.simscoreservice.analytics.dto;

import com.sims.simscoreservice.analytics.enums.TimeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Financial Overview Metrics
 * Revenue, profit, and financial health metrics
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinancialOverviewMetrics {

    private BigDecimal totalRevenue;
    private BigDecimal avgOrderValue;
    private BigDecimal lossValue;
    private BigDecimal profitMargin;    // Percentage
    private BigDecimal netProfit;

    // Period information
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private TimeRange timeRange;
    private Long daysInPeriod;

    /**
     * Calculate loss percentage
     */
    public BigDecimal getLossPercentage() {
        return totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? lossValue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
    }

    /**
     * Calculate average revenue per day
     */
    public BigDecimal getAvgRevenuePerDay() {
        if (daysInPeriod == null || daysInPeriod == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(daysInPeriod), 2, RoundingMode.HALF_UP);
    }
}
