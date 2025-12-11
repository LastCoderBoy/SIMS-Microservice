package com.sims.simscoreservice.analytics.service;

import com.sims.simscoreservice.analytics.dto.*;
import com.sims.simscoreservice.analytics.enums.TimeRange;

import java.time.LocalDate;

/**
 * Report Analytics Service
 * Main service for all analytics operations
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface ReportAnalyticsService {

    DashboardMetrics getMainDashboardMetrics();

    /**
     * Get inventory health metrics
     */
    InventoryReportMetrics getInventoryHealth();

    /**
     * Get financial overview by time range
     */
    FinancialOverviewMetrics getFinancialOverview(TimeRange timeRange);

    /**
     * Get financial overview by custom date range
     */
    FinancialOverviewMetrics getFinancialOverview(LocalDate startDate, LocalDate endDate);

    /**
     * Get order summary metrics
     */
    OrderSummaryMetrics getOrderSummary();
}
