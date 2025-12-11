package com.sims.simscoreservice.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order Summary Metrics
 * Combined sales and purchase order summaries
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryMetrics {
    private SalesOrderSummary salesOrderSummary;
    private PurchaseOrderSummary purchaseOrderSummary;
}
