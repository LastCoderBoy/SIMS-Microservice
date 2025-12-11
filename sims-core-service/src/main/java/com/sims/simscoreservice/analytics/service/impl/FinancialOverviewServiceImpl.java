package com.sims.simscoreservice.analytics.service.impl;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.simscoreservice.analytics.dto.FinancialOverviewMetrics;
import com.sims.simscoreservice.analytics.enums.TimeRange;
import com.sims.simscoreservice.analytics.service.FinancialOverviewService;
import com.sims.simscoreservice.inventory.repository.DamageLossRepository;
import com.sims.simscoreservice.salesOrder.repository.OrderItemRepository;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Financial Overview Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialOverviewServiceImpl implements FinancialOverviewService {

    private final OrderItemRepository orderItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final DamageLossRepository damageLossRepository;

    // Cost estimation (30% of revenue as cost)
    private static final BigDecimal COST_PERCENTAGE = BigDecimal.valueOf(0.30);

    @Override
    @Transactional(readOnly = true)
    public FinancialOverviewMetrics getFinancialOverview(TimeRange timeRange) {
        try {
            if (timeRange == null) {
                throw new ValidationException("Time range cannot be null");
            }
            if (timeRange == TimeRange.CUSTOM) {
                throw new ValidationException("Please provide start and end dates for custom range");
            }

            LocalDate startDate = timeRange.getStartDate();
            LocalDate endDate = timeRange.getEndDate();

            log.info("[ANALYTICS-FIN] Fetching {} financial overview from {} to {}",
                    timeRange.getDisplayName(), startDate, endDate);

            return calculateFinancialMetrics(startDate, endDate, timeRange);

        } catch (ValidationException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[ANALYTICS-FIN] Database error:  {}", e.getMessage(), e);
            throw new DatabaseException("Failed to fetch financial overview", e);
        } catch (Exception e) {
            log.error("[ANALYTICS-FIN] Unexpected error:  {}", e.getMessage(), e);
            throw new ServiceException("Failed to fetch financial overview", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialOverviewMetrics getFinancialOverview(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                throw new ValidationException("Start date and end date are required for custom range");
            }
            if (startDate.isAfter(endDate)) {
                throw new ValidationException("Start date must be before or equal to end date");
            }

            log.info("[ANALYTICS-FIN] Fetching custom financial overview from {} to {}",
                    startDate, endDate);

            return calculateFinancialMetrics(startDate, endDate, TimeRange.CUSTOM);

        } catch (ValidationException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("[ANALYTICS-FIN] Database error: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to fetch financial overview", e);
        } catch (Exception e) {
            log.error("[ANALYTICS-FIN] Unexpected error: {}", e.getMessage(), e);
            throw new ServiceException("Failed to fetch financial overview", e);
        }
    }

    /**
     * Calculate financial metrics for given date range
     */
    private FinancialOverviewMetrics calculateFinancialMetrics(
            LocalDate startDate, LocalDate endDate, TimeRange timeRange) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // Fetch financial data
        BigDecimal totalRevenue = orderItemRepository.calculateTotalRevenue(start, end);
        Long totalCompletedOrders = countCompletedSalesOrders(start, end);

        // Calculate average order value
        BigDecimal avgOrderValue = (totalCompletedOrders == 0)
                ? BigDecimal.ZERO
                : totalRevenue.divide(
                BigDecimal.valueOf(totalCompletedOrders),
                2,
                RoundingMode.HALF_UP
        );

        // Get loss value
        BigDecimal lossValue = damageLossRepository.sumLossValueBetween(start, end);

        // Calculate profit metrics
        BigDecimal estimatedCost = totalRevenue.multiply(COST_PERCENTAGE);
        BigDecimal grossProfitBeforeLoss = totalRevenue.subtract(estimatedCost);
        BigDecimal netProfitAfterLoss = grossProfitBeforeLoss.subtract(lossValue);

        // Calculate profit margin percentage
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfitAfterLoss
                .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Calculate days in period
        long daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        log.info("[ANALYTICS-FIN] Financial Overview - Revenue: ${}, Net Profit: ${}, Margin: {}%",
                totalRevenue, netProfitAfterLoss, profitMargin);

        return FinancialOverviewMetrics.builder()
                .totalRevenue(totalRevenue)
                .avgOrderValue(avgOrderValue)
                .lossValue(lossValue)
                .profitMargin(profitMargin)
                .netProfit(netProfitAfterLoss)
                .periodStart(startDate)
                .periodEnd(endDate)
                .timeRange(timeRange)
                .daysInPeriod(daysInPeriod)
                .build();
    }

    /**
     * Count completed sales orders in date range
     */
    private Long countCompletedSalesOrders(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return salesOrderRepository.countCompletedSalesOrdersBetween(startDate, endDate);
        } catch (DataAccessException e) {
            log.error("[ANALYTICS-FIN] Database error counting completed orders: {}", e.getMessage(), e);
            throw new DatabaseException("Failed to count completed sales orders", e);
        }
    }
}
