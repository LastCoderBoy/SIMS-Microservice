package com.sims.simscoreservice.analytics.service;


import com.sims.simscoreservice.analytics.dto.FinancialOverviewMetrics;
import com.sims.simscoreservice.analytics.enums.TimeRange;

import java.time.LocalDate;

/**
 * Financial Overview Service
 * Provides financial metrics and revenue analysis
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface FinancialOverviewService {

    /**
     * Get financial overview by predefined time range
     */
    FinancialOverviewMetrics getFinancialOverview(TimeRange timeRange);

    /**
     * Get financial overview by custom date range
     */
    FinancialOverviewMetrics getFinancialOverview(LocalDate startDate, LocalDate endDate);
}
