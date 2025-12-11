package com.sims.simscoreservice.analytics.service;

import com.sims.simscoreservice.analytics.dto.OrderSummaryMetrics;
import com.sims.simscoreservice.analytics.dto.PurchaseOrderSummary;
import com.sims.simscoreservice.analytics.dto.SalesOrderSummary;

/**
 * Order Summary Service
 * Provides order statistics and summaries
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface OrderSummaryService {

    /**
     * Get complete order summary (both sales and purchase orders)
     */
    OrderSummaryMetrics getOrderSummaryMetrics();

    SalesOrderSummary getSalesOrderSummary();

    PurchaseOrderSummary getPurchaseOrderSummary();
}
