package com.sims.simscoreservice.analytics.service.impl;

import com.sims.simscoreservice.analytics.dto.DashboardMetrics;
import com.sims.simscoreservice.analytics.dto.FinancialOverviewMetrics;
import com.sims.simscoreservice.analytics.dto.InventoryReportMetrics;
import com.sims.simscoreservice.analytics.dto.OrderSummaryMetrics;
import com.sims.simscoreservice.analytics.enums.TimeRange;
import com.sims.simscoreservice.analytics.service.FinancialOverviewService;
import com.sims.simscoreservice.analytics.service.InventoryHealthService;
import com.sims.simscoreservice.analytics.service.OrderSummaryService;
import com.sims.simscoreservice.analytics.service.ReportAnalyticsService;
import com.sims.simscoreservice.inventory.queryService.DamageLossQueryService;
import com.sims.simscoreservice.product.dto.ProductReportMetrics;
import com.sims.simscoreservice.product.services.queryService.ProductQueryService;
import com.sims.simscoreservice.purchaseOrder.queryService.PurchaseOrderQueryService;
import com.sims.simscoreservice.salesOrder.queryService.SalesOrderQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Report Analytics Service Implementation
 * Main orchestration service for all analytics
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportAnalyticsServiceImpl implements ReportAnalyticsService {

    // Query Services
    private final ProductQueryService productQueryService;
    private final SalesOrderQueryService salesOrderQueryService;
    private final PurchaseOrderQueryService purchaseOrderQueryService;
    private final DamageLossQueryService damageLossQueryService;

    // Analytics Services
    private final InventoryHealthService inventoryHealthService;
    private final OrderSummaryService orderSummaryService;
    private final FinancialOverviewService financialOverviewService;

    @Override
    @Transactional(readOnly = true)
    public DashboardMetrics getMainDashboardMetrics() {
        try {
            log.info("[ANALYTICS] Fetching main dashboard metrics");

            // Gather metrics from different services
            ProductReportMetrics productMetrics = productQueryService.countTotalActiveInactiveProducts();
            BigDecimal inventoryStockValue = inventoryHealthService.calculateInventoryStockValueAtRetail();
            Long inProgressSalesOrders = salesOrderQueryService.countInProgressSalesOrders();
            Long totalValidPurchaseOrders = purchaseOrderQueryService.getTotalValidPoSize();
            Long totalDamagedProducts = damageLossQueryService.countTotalDamagedProducts();

            log.info("[ANALYTICS] Dashboard - Active Products: {}, Inventory Value: ${}, In-Progress SO: {}",
                    productMetrics.getTotalActiveProducts(),
                    inventoryStockValue,
                    inProgressSalesOrders);

            return DashboardMetrics.builder()
                    .totalActiveProducts(productMetrics.getTotalActiveProducts())
                    .totalInactiveProducts(productMetrics.getTotalInactiveProducts())
                    .totalInventoryStockValue(inventoryStockValue)
                    .totalInProgressSalesOrders(inProgressSalesOrders)
                    .totalValidPurchaseOrders(totalValidPurchaseOrders)
                    .totalDamagedProducts(totalDamagedProducts)
                    .build();

        } catch (Exception e) {
            log.error("[ANALYTICS] Error fetching dashboard metrics: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryReportMetrics getInventoryHealth() {
        log.info("[ANALYTICS] Delegating to InventoryHealthService");
        return inventoryHealthService.getInventoryHealth();
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialOverviewMetrics getFinancialOverview(TimeRange timeRange) {
        log.info("[ANALYTICS] Delegating to FinancialOverviewService with TimeRange: {}", timeRange);
        return financialOverviewService.getFinancialOverview(timeRange);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialOverviewMetrics getFinancialOverview(LocalDate startDate, LocalDate endDate) {
        log.info("[ANALYTICS] Delegating to FinancialOverviewService with custom dates: {} to {}",
                startDate, endDate);
        return financialOverviewService.getFinancialOverview(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryMetrics getOrderSummary() {
        log.info("[ANALYTICS] Delegating to OrderSummaryService");
        return orderSummaryService.getOrderSummaryMetrics();
    }
}
