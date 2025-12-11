package com.sims.simscoreservice.analytics.service;


import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.analytics.dto.FinancialOverviewMetrics;
import com.sims.simscoreservice.analytics.enums.TimeRange;
import com.sims.simscoreservice.analytics.service.impl.FinancialOverviewServiceImpl;
import com.sims.simscoreservice.inventory.repository.DamageLossRepository;
import com.sims.simscoreservice.salesOrder.repository.OrderItemRepository;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Financial Overview Service Tests
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Financial Overview Service Tests")
class FinancialOverviewServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private DamageLossRepository damageLossRepository;

    @InjectMocks
    private FinancialOverviewServiceImpl financialOverviewService;

    @BeforeEach
    void setUp() {
        // Mock repository responses
        when(orderItemRepository.calculateTotalRevenue(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(100000.00));

        when(salesOrderRepository.countCompletedSalesOrdersBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(40L);

        when(damageLossRepository.sumLossValueBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(5000.00));
    }

    // ========================================
    // TIME RANGE TESTS
    // ========================================

    @Test
    @DisplayName("Should calculate financial overview for MONTHLY range")
    void getFinancialOverview_Monthly_Success() {
        // Act
        FinancialOverviewMetrics result = financialOverviewService.getFinancialOverview(TimeRange.MONTHLY);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(100000.00));
        assertThat(result.getAvgOrderValue()).isEqualByComparingTo(BigDecimal.valueOf(2500.00));
        assertThat(result.getLossValue()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(result.getTimeRange()).isEqualTo(TimeRange.MONTHLY);
        assertThat(result.getPeriodStart()).isEqualTo(LocalDate.now().withDayOfMonth(1));

        verify(orderItemRepository).calculateTotalRevenue(any(), any());
        verify(salesOrderRepository).countCompletedSalesOrdersBetween(any(), any());
        verify(damageLossRepository).sumLossValueBetween(any(), any());
    }

    @Test
    @DisplayName("Should calculate financial overview for YEARLY range")
    void getFinancialOverview_Yearly_Success() {
        // Act
        FinancialOverviewMetrics result = financialOverviewService.getFinancialOverview(TimeRange.YEARLY);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTimeRange()).isEqualTo(TimeRange.YEARLY);
        assertThat(result.getPeriodStart()).isEqualTo(LocalDate.now().withDayOfYear(1));
    }

    @Test
    @DisplayName("Should throw ValidationException for CUSTOM range without dates")
    void getFinancialOverview_Custom_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> financialOverviewService.getFinancialOverview(TimeRange.CUSTOM))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Please provide start and end dates");
    }

    @Test
    @DisplayName("Should throw ValidationException for null time range")
    void getFinancialOverview_NullRange_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> financialOverviewService.getFinancialOverview((TimeRange) null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Time range cannot be null");
    }

    // ========================================
    // CUSTOM DATE RANGE TESTS
    // ========================================

    @Test
    @DisplayName("Should calculate financial overview for custom date range")
    void getFinancialOverview_CustomDates_Success() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 15);

        // Act
        FinancialOverviewMetrics result = financialOverviewService.getFinancialOverview(startDate, endDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPeriodStart()).isEqualTo(startDate);
        assertThat(result.getPeriodEnd()).isEqualTo(endDate);
        assertThat(result.getDaysInPeriod()).isEqualTo(15L);
        assertThat(result.getTimeRange()).isEqualTo(TimeRange.CUSTOM);
    }

    @Test
    @DisplayName("Should throw ValidationException when start date is after end date")
    void getFinancialOverview_InvalidDateRange_ThrowsException() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 1, 15);
        LocalDate endDate = LocalDate.of(2025, 1, 1);

        // Act & Assert
        assertThatThrownBy(() -> financialOverviewService.getFinancialOverview(startDate, endDate))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start date must be before or equal to end date");
    }

    @Test
    @DisplayName("Should throw ValidationException when dates are null")
    void getFinancialOverview_NullDates_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> financialOverviewService.getFinancialOverview(null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start date and end date are required");
    }

    // ========================================
    // CALCULATION TESTS
    // ========================================

    @Test
    @DisplayName("Should calculate profit margin correctly")
    void calculateFinancialMetrics_ProfitMargin_CorrectCalculation() {
        // Act
        FinancialOverviewMetrics result = financialOverviewService.getFinancialOverview(TimeRange.MONTHLY);

        // Assert
        // Revenue: 100,000
        // Cost (30%): 30,000
        // Gross Profit: 70,000
        // Loss:  5,000
        // Net Profit: 65,000
        // Profit Margin: (65,000 / 100,000) * 100 = 65%
        assertThat(result.getNetProfit()).isEqualByComparingTo(BigDecimal.valueOf(65000.00));
        assertThat(result.getProfitMargin()).isEqualByComparingTo(BigDecimal.valueOf(65.0));
    }

    @Test
    @DisplayName("Should calculate average order value correctly")
    void calculateFinancialMetrics_AvgOrderValue_CorrectCalculation() {
        // Act
        FinancialOverviewMetrics result = financialOverviewService.getFinancialOverview(TimeRange.MONTHLY);

        // Assert
        // Total Revenue: 100,000
        // Completed Orders: 40
        // Avg Order Value: 100,000 / 40 = 2,500
        assertThat(result.getAvgOrderValue()).isEqualByComparingTo(BigDecimal.valueOf(2500.00));
    }

    @Test
    @DisplayName("Should handle zero revenue correctly")
    void calculateFinancialMetrics_ZeroRevenue_HandlesCorrectly() {
        // Arrange
        when(orderItemRepository.calculateTotalRevenue(any(), any()))
                .thenReturn(BigDecimal.ZERO);

        // Act
        FinancialOverviewMetrics result = financialOverviewService.getFinancialOverview(TimeRange.MONTHLY);

        // Assert
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAvgOrderValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getProfitMargin()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate avg revenue per day correctly")
    void getAvgRevenuePerDay_CorrectCalculation() {
        // Act
        FinancialOverviewMetrics result = financialOverviewService.getFinancialOverview(TimeRange.MONTHLY);

        // Assert
        // Assuming 31 days in month
        BigDecimal expectedAvgPerDay = BigDecimal.valueOf(100000.00)
                .divide(BigDecimal.valueOf(result.getDaysInPeriod()), 2, java.math.RoundingMode.HALF_UP);

        assertThat(result.getAvgRevenuePerDay()).isEqualByComparingTo(expectedAvgPerDay);
    }
}
