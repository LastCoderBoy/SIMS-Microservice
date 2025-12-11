package com.sims.simscoreservice.analytics.dto;

import lombok. Builder;

import java.math.BigDecimal;

/**
 * Dashboard Metrics
 * Main dashboard overview with key metrics
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Builder
public record DashboardMetrics(
        Long totalActiveProducts,
        Long totalInactiveProducts,
        BigDecimal totalInventoryStockValue,
        Long totalInProgressSalesOrders,
        Long totalValidPurchaseOrders,
        Long totalDamagedProducts
) {}
