package com.sims.simscoreservice.analytics.service;

import com.sims.simscoreservice.analytics.dto.*;
import com.sims.simscoreservice.analytics.enums.TimeRange;
import com.sims.simscoreservice.analytics.service.impl.ReportAnalyticsServiceImpl;
import com.sims.simscoreservice.inventory.queryService.DamageLossQueryService;
import com.sims.simscoreservice.product.dto.ProductReportMetrics;
import com.sims.simscoreservice.product.services.queryService.ProductQueryService;
import com.sims.simscoreservice.purchaseOrder.queryService.PurchaseOrderQueryService;
import com.sims.simscoreservice.salesOrder.queryService.SalesOrderQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Report Analytics Service Tests
 * Tests the main orchestration service
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Report Analytics Service Tests")
class ReportAnalyticsServiceTest {

    @Mock
    private ProductQueryService productQueryService;

    @Mock
    private SalesOrderQueryService salesOrderQueryService;

    @Mock
    private PurchaseOrderQueryService purchaseOrderQueryService;

    @Mock
    private DamageLossQueryService damageLossQueryService;

    @Mock
    private InventoryHealthService inventoryHealthService;

    @Mock
    private OrderSummaryService orderSummaryService;

    @Mock
    private FinancialOverviewService financialOverviewService;

    @InjectMocks
    private ReportAnalyticsServiceImpl reportAnalyticsService;

    private ProductReportMetrics mockProductMetrics;
    private InventoryReportMetrics mockInventoryMetrics;
    private FinancialOverviewMetrics mockFinancialMetrics;
    private OrderSummaryMetrics mockOrderSummary;

    @BeforeEach
    void setUp() {
        // Mock Product Metrics
        mockProductMetrics = ProductReportMetrics.builder()
                .totalActiveProducts(150L)
                .totalInactiveProducts(20L)
                .build();

        // Mock Inventory Metrics
        mockInventoryMetrics = InventoryReportMetrics.builder()
                .totalStockValueAtRetail(BigDecimal.valueOf(500000.00))
                .totalStockQuantity(1000L)
                .totalReservedStock(200L)
                .availableStock(800L)
                .inStockItems(120L)
                .lowStockItems(15L)
                .outOfStockItems(5L)
                .build();

        // Mock Financial Metrics
        mockFinancialMetrics = FinancialOverviewMetrics.builder()
                .totalRevenue(BigDecimal.valueOf(100000.00))
                .avgOrderValue(BigDecimal.valueOf(2500.00))
                .lossValue(BigDecimal.valueOf(5000.00))
                .profitMargin(BigDecimal.valueOf(65.0))
                .netProfit(BigDecimal.valueOf(65000.00))
                .periodStart(LocalDate.of(2025, 1, 1))
                .periodEnd(LocalDate.of(2025, 1, 31))
                .timeRange(TimeRange.MONTHLY)
                .daysInPeriod(31L)
                .build();

        // Mock Order Summary
        SalesOrderSummary salesSummary = SalesOrderSummary.builder()
                .totalPending(10L)
                .totalApproved(15L)
                .totalDelivered(50L)
                .totalCancelled(5L)
                .totalDeliveryInProcess(0L)
                .totalPartiallyApproved(0L)
                .totalPartiallyDelivered(0L)
                .build();

        PurchaseOrderSummary purchaseSummary = PurchaseOrderSummary.builder()
                .totalAwaitingApproval(8L)
                .totalDeliveryInProcess(5L)
                .totalReceived(30L)
                .totalCancelled(2L)
                .totalPartiallyReceived(0L)
                .totalFailed(0L)
                .build();

        mockOrderSummary = OrderSummaryMetrics.builder()
                .salesOrderSummary(salesSummary)
                .purchaseOrderSummary(purchaseSummary)
                .build();
    }

    // ========================================
    // DASHBOARD METRICS TESTS
    // ========================================

    @Test
    @DisplayName("Should successfully get main dashboard metrics")
    void getMainDashboardMetrics_Success() {
        // Arrange
        when(productQueryService.countTotalActiveInactiveProducts()).thenReturn(mockProductMetrics);
        when(inventoryHealthService.calculateInventoryStockValueAtRetail())
                .thenReturn(BigDecimal.valueOf(500000.00));
        when(salesOrderQueryService.countInProgressSalesOrders()).thenReturn(25L);
        when(purchaseOrderQueryService.getTotalValidPoSize()).thenReturn(10L);
        when(damageLossQueryService.countTotalDamagedProducts()).thenReturn(5L);

        // Act
        DashboardMetrics result = reportAnalyticsService.getMainDashboardMetrics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.totalActiveProducts()).isEqualTo(150L);
        assertThat(result.totalInactiveProducts()).isEqualTo(20L);
        assertThat(result.totalInventoryStockValue()).isEqualByComparingTo(BigDecimal.valueOf(500000.00));
        assertThat(result.totalInProgressSalesOrders()).isEqualTo(25L);
        assertThat(result.totalValidPurchaseOrders()).isEqualTo(10L);
        assertThat(result.totalDamagedProducts()).isEqualTo(5L);

        // Verify all services were called
        verify(productQueryService).countTotalActiveInactiveProducts();
        verify(inventoryHealthService).calculateInventoryStockValueAtRetail();
        verify(salesOrderQueryService).countInProgressSalesOrders();
        verify(purchaseOrderQueryService).getTotalValidPoSize();
        verify(damageLossQueryService).countTotalDamagedProducts();
    }

    @Test
    @DisplayName("Should handle exception when fetching dashboard metrics")
    void getMainDashboardMetrics_ExceptionThrown() {
        // Arrange
        when(productQueryService.countTotalActiveInactiveProducts())
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatThrownBy(() -> reportAnalyticsService.getMainDashboardMetrics())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        verify(productQueryService).countTotalActiveInactiveProducts();
        verify(inventoryHealthService, never()).calculateInventoryStockValueAtRetail();
    }

    @Test
    @DisplayName("Should aggregate metrics from multiple services correctly")
    void getMainDashboardMetrics_AggregatesCorrectly() {
        // Arrange
        ProductReportMetrics customProductMetrics = ProductReportMetrics.builder()
                .totalActiveProducts(200L)
                .totalInactiveProducts(50L)
                .build();

        when(productQueryService.countTotalActiveInactiveProducts()).thenReturn(customProductMetrics);
        when(inventoryHealthService.calculateInventoryStockValueAtRetail())
                .thenReturn(BigDecimal.valueOf(750000.00));
        when(salesOrderQueryService.countInProgressSalesOrders()).thenReturn(30L);
        when(purchaseOrderQueryService.getTotalValidPoSize()).thenReturn(15L);
        when(damageLossQueryService.countTotalDamagedProducts()).thenReturn(8L);

        // Act
        DashboardMetrics result = reportAnalyticsService.getMainDashboardMetrics();

        // Assert
        assertThat(result.totalActiveProducts()).isEqualTo(200L);
        assertThat(result.totalInactiveProducts()).isEqualTo(50L);
        assertThat(result.totalInventoryStockValue()).isEqualByComparingTo(BigDecimal.valueOf(750000.00));
        assertThat(result.totalInProgressSalesOrders()).isEqualTo(30L);
        assertThat(result.totalValidPurchaseOrders()).isEqualTo(15L);
        assertThat(result.totalDamagedProducts()).isEqualTo(8L);
    }

    @Test
    @DisplayName("Should handle zero values in dashboard metrics")
    void getMainDashboardMetrics_ZeroValues_HandlesCorrectly() {
        // Arrange
        ProductReportMetrics zeroProductMetrics = ProductReportMetrics.builder()
                .totalActiveProducts(0L)
                .totalInactiveProducts(0L)
                .build();

        when(productQueryService.countTotalActiveInactiveProducts()).thenReturn(zeroProductMetrics);
        when(inventoryHealthService.calculateInventoryStockValueAtRetail()).thenReturn(BigDecimal.ZERO);
        when(salesOrderQueryService.countInProgressSalesOrders()).thenReturn(0L);
        when(purchaseOrderQueryService.getTotalValidPoSize()).thenReturn(0L);
        when(damageLossQueryService.countTotalDamagedProducts()).thenReturn(0L);

        // Act
        DashboardMetrics result = reportAnalyticsService.getMainDashboardMetrics();

        // Assert
        assertThat(result.totalActiveProducts()).isEqualTo(0L);
        assertThat(result.totalInventoryStockValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalInProgressSalesOrders()).isEqualTo(0L);
    }

    // ========================================
    // INVENTORY HEALTH TESTS
    // ========================================

    @Test
    @DisplayName("Should delegate to InventoryHealthService successfully")
    void getInventoryHealth_Success() {
        // Arrange
        when(inventoryHealthService.getInventoryHealth()).thenReturn(mockInventoryMetrics);

        // Act
        InventoryReportMetrics result = reportAnalyticsService.getInventoryHealth();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockInventoryMetrics);
        assertThat(result.getTotalStockQuantity()).isEqualTo(1000L);
        assertThat(result.getTotalReservedStock()).isEqualTo(200L);
        assertThat(result.getHealthStatus()).isEqualTo("EXCELLENT");

        verify(inventoryHealthService).getInventoryHealth();
    }

    @Test
    @DisplayName("Should propagate exception from InventoryHealthService")
    void getInventoryHealth_ExceptionPropagated() {
        // Arrange
        when(inventoryHealthService.getInventoryHealth())
                .thenThrow(new RuntimeException("Inventory service error"));

        // Act & Assert
        assertThatThrownBy(() -> reportAnalyticsService.getInventoryHealth())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Inventory service error");

        verify(inventoryHealthService).getInventoryHealth();
    }

    // ========================================
    // FINANCIAL OVERVIEW TESTS (TIME RANGE)
    // ========================================

    @Test
    @DisplayName("Should delegate to FinancialOverviewService with TimeRange successfully")
    void getFinancialOverview_TimeRange_Success() {
        // Arrange
        when(financialOverviewService.getFinancialOverview(TimeRange.MONTHLY))
                .thenReturn(mockFinancialMetrics);

        // Act
        FinancialOverviewMetrics result = reportAnalyticsService.getFinancialOverview(TimeRange.MONTHLY);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockFinancialMetrics);
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100000.00));
        assertThat(result.getTimeRange()).isEqualTo(TimeRange.MONTHLY);

        verify(financialOverviewService).getFinancialOverview(TimeRange.MONTHLY);
    }

    @Test
    @DisplayName("Should delegate YEARLY time range correctly")
    void getFinancialOverview_YearlyRange_Success() {
        // Arrange
        FinancialOverviewMetrics yearlyMetrics = FinancialOverviewMetrics.builder()
                .totalRevenue(BigDecimal.valueOf(1200000.00))
                .timeRange(TimeRange.YEARLY)
                .build();

        when(financialOverviewService.getFinancialOverview(TimeRange.YEARLY))
                .thenReturn(yearlyMetrics);

        // Act
        FinancialOverviewMetrics result = reportAnalyticsService.getFinancialOverview(TimeRange.YEARLY);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(1200000.00));
        assertThat(result.getTimeRange()).isEqualTo(TimeRange.YEARLY);

        verify(financialOverviewService).getFinancialOverview(TimeRange.YEARLY);
    }

    @Test
    @DisplayName("Should propagate exception from FinancialOverviewService (TimeRange)")
    void getFinancialOverview_TimeRange_ExceptionPropagated() {
        // Arrange
        when(financialOverviewService.getFinancialOverview(TimeRange.MONTHLY))
                .thenThrow(new RuntimeException("Financial service error"));

        // Act & Assert
        assertThatThrownBy(() -> reportAnalyticsService.getFinancialOverview(TimeRange.MONTHLY))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Financial service error");

        verify(financialOverviewService).getFinancialOverview(TimeRange.MONTHLY);
    }

    // ========================================
    // FINANCIAL OVERVIEW TESTS (CUSTOM DATES)
    // ========================================

    @Test
    @DisplayName("Should delegate to FinancialOverviewService with custom dates successfully")
    void getFinancialOverview_CustomDates_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 15);

        FinancialOverviewMetrics customMetrics = FinancialOverviewMetrics.builder()
                .totalRevenue(BigDecimal.valueOf(50000.00))
                .periodStart(startDate)
                .periodEnd(endDate)
                .timeRange(TimeRange.CUSTOM)
                .daysInPeriod(15L)
                .build();

        when(financialOverviewService.getFinancialOverview(startDate, endDate))
                .thenReturn(customMetrics);

        // Act
        FinancialOverviewMetrics result = reportAnalyticsService.getFinancialOverview(startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(50000.00));
        assertThat(result.getPeriodStart()).isEqualTo(startDate);
        assertThat(result.getPeriodEnd()).isEqualTo(endDate);
        assertThat(result.getTimeRange()).isEqualTo(TimeRange.CUSTOM);
        assertThat(result.getDaysInPeriod()).isEqualTo(15L);

        verify(financialOverviewService).getFinancialOverview(startDate, endDate);
    }

    @Test
    @DisplayName("Should propagate exception from FinancialOverviewService (Custom dates)")
    void getFinancialOverview_CustomDates_ExceptionPropagated() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 15);

        when(financialOverviewService.getFinancialOverview(startDate, endDate))
                .thenThrow(new RuntimeException("Custom date range error"));

        // Act & Assert
        assertThatThrownBy(() -> reportAnalyticsService.getFinancialOverview(startDate, endDate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Custom date range error");

        verify(financialOverviewService).getFinancialOverview(startDate, endDate);
    }

    // ========================================
    // ORDER SUMMARY TESTS
    // ========================================

    @Test
    @DisplayName("Should delegate to OrderSummaryService successfully")
    void getOrderSummary_Success() {
        // Arrange
        when(orderSummaryService.getOrderSummaryMetrics()).thenReturn(mockOrderSummary);

        // Act
        OrderSummaryMetrics result = reportAnalyticsService.getOrderSummary();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockOrderSummary);
        assertThat(result.getSalesOrderSummary()).isNotNull();
        assertThat(result.getSalesOrderSummary().getTotalDelivered()).isEqualTo(50L);
        assertThat(result.getPurchaseOrderSummary()).isNotNull();
        assertThat(result.getPurchaseOrderSummary().getTotalReceived()).isEqualTo(30L);

        verify(orderSummaryService).getOrderSummaryMetrics();
    }

    @Test
    @DisplayName("Should verify sales and purchase order summaries are populated")
    void getOrderSummary_BothSummariesPopulated() {
        // Arrange
        when(orderSummaryService.getOrderSummaryMetrics()).thenReturn(mockOrderSummary);

        // Act
        OrderSummaryMetrics result = reportAnalyticsService.getOrderSummary();

        // Assert - Sales Order Summary
        assertThat(result.getSalesOrderSummary().getTotalPending()).isEqualTo(10L);
        assertThat(result.getSalesOrderSummary().getTotalApproved()).isEqualTo(15L);
        assertThat(result.getSalesOrderSummary().getTotalDelivered()).isEqualTo(50L);
        assertThat(result.getSalesOrderSummary().getTotalCancelled()).isEqualTo(5L);

        // Assert - Purchase Order Summary
        assertThat(result.getPurchaseOrderSummary().getTotalAwaitingApproval()).isEqualTo(8L);
        assertThat(result.getPurchaseOrderSummary().getTotalDeliveryInProcess()).isEqualTo(5L);
        assertThat(result.getPurchaseOrderSummary().getTotalReceived()).isEqualTo(30L);
        assertThat(result.getPurchaseOrderSummary().getTotalCancelled()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should propagate exception from OrderSummaryService")
    void getOrderSummary_ExceptionPropagated() {
        // Arrange
        when(orderSummaryService.getOrderSummaryMetrics())
                .thenThrow(new RuntimeException("Order summary service error"));

        // Act & Assert
        assertThatThrownBy(() -> reportAnalyticsService.getOrderSummary())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order summary service error");

        verify(orderSummaryService).getOrderSummaryMetrics();
    }

    // ========================================
    // INTEGRATION/ORCHESTRATION TESTS
    // ========================================

    @Test
    @DisplayName("Should orchestrate multiple service calls for complete analytics")
    void orchestrateMultipleServices_Success() {
        // Arrange
        when(productQueryService.countTotalActiveInactiveProducts()).thenReturn(mockProductMetrics);
        when(inventoryHealthService.calculateInventoryStockValueAtRetail())
                .thenReturn(BigDecimal.valueOf(500000.00));
        when(salesOrderQueryService.countInProgressSalesOrders()).thenReturn(25L);
        when(purchaseOrderQueryService.getTotalValidPoSize()).thenReturn(10L);
        when(damageLossQueryService.countTotalDamagedProducts()).thenReturn(5L);
        when(inventoryHealthService.getInventoryHealth()).thenReturn(mockInventoryMetrics);
        when(financialOverviewService.getFinancialOverview(TimeRange.MONTHLY)).thenReturn(mockFinancialMetrics);
        when(orderSummaryService.getOrderSummaryMetrics()).thenReturn(mockOrderSummary);

        // Act - Simulate a complete analytics fetch
        DashboardMetrics dashboard = reportAnalyticsService.getMainDashboardMetrics();
        InventoryReportMetrics inventory = reportAnalyticsService.getInventoryHealth();
        FinancialOverviewMetrics financial = reportAnalyticsService.getFinancialOverview(TimeRange.MONTHLY);
        OrderSummaryMetrics orders = reportAnalyticsService.getOrderSummary();

        // Assert - All metrics retrieved successfully
        assertThat(dashboard).isNotNull();
        assertThat(inventory).isNotNull();
        assertThat(financial).isNotNull();
        assertThat(orders).isNotNull();

        // Verify all services called
        verify(productQueryService).countTotalActiveInactiveProducts();
        verify(inventoryHealthService).calculateInventoryStockValueAtRetail();
        verify(inventoryHealthService).getInventoryHealth();
        verify(financialOverviewService).getFinancialOverview(TimeRange.MONTHLY);
        verify(orderSummaryService).getOrderSummaryMetrics();
    }

    @Test
    @DisplayName("Should handle partial service failure gracefully")
    void partialServiceFailure_OtherServicesStillWork() {
        // Arrange - Inventory service fails, but others succeed
        when(productQueryService.countTotalActiveInactiveProducts()).thenReturn(mockProductMetrics);
        when(inventoryHealthService.calculateInventoryStockValueAtRetail())
                .thenThrow(new RuntimeException("Inventory service down"));

        // Act & Assert - Dashboard should fail due to inventory
        assertThatThrownBy(() -> reportAnalyticsService.getMainDashboardMetrics())
                .isInstanceOf(RuntimeException.class);

        // But other services should still work independently
        when(financialOverviewService.getFinancialOverview(TimeRange.MONTHLY)).thenReturn(mockFinancialMetrics);
        assertThatCode(() -> reportAnalyticsService.getFinancialOverview(TimeRange.MONTHLY))
                .doesNotThrowAnyException();
    }
}
