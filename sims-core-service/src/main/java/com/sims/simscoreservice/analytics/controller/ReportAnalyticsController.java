package com.sims.simscoreservice.analytics.controller;

import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.analytics.dto.DashboardMetrics;
import com.sims.simscoreservice.analytics.dto.FinancialOverviewMetrics;
import com.sims.simscoreservice.analytics.dto.InventoryReportMetrics;
import com.sims.simscoreservice.analytics.dto.OrderSummaryMetrics;
import com.sims.simscoreservice.analytics.enums.TimeRange;
import com.sims.simscoreservice.analytics.service.ReportAnalyticsService;
import com.sims.simscoreservice.shared.util.RoleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.sims.common.constants.AppConstants.BASE_ANALYTICS_PATH;
import static com.sims.common.constants.AppConstants.USER_ROLES_HEADER;

/**
 * Report & Analytics Controller
 * Provides dashboard and analytics endpoints
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_ANALYTICS_PATH)
@RequiredArgsConstructor
@Slf4j
public class ReportAnalyticsController {

    private final ReportAnalyticsService reportAnalyticsService;
    private final RoleValidator roleValidator;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardMetrics>> getMainDashboard(
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        log.info("[ANALYTICS-CONTROLLER] Main dashboard requested");

        DashboardMetrics metrics = reportAnalyticsService.getMainDashboardMetrics();

        return ResponseEntity.ok(ApiResponse.success(
                "Main dashboard retrieved successfully",
                metrics
        ));
    }

    @GetMapping("/inventory-health")
    public ResponseEntity<ApiResponse<InventoryReportMetrics>> getInventoryHealth(
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        log.info("[ANALYTICS-CONTROLLER] Inventory health requested");

        InventoryReportMetrics metrics = reportAnalyticsService.getInventoryHealth();

        return ResponseEntity.ok(ApiResponse.success(
                "Inventory health retrieved successfully",
                metrics
        ));
    }

    @GetMapping("/financial-overview")
    public ResponseEntity<ApiResponse<FinancialOverviewMetrics>> getFinancialOverview(
            @RequestHeader(USER_ROLES_HEADER) String roles,
            @RequestParam(required = false, defaultValue = "MONTHLY") TimeRange range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        log.info("[ANALYTICS-CONTROLLER] Financial overview requested with range: {}", range);

        FinancialOverviewMetrics metrics;

        if (range == TimeRange.CUSTOM) {
            metrics = reportAnalyticsService.getFinancialOverview(startDate, endDate);
        } else {
            metrics = reportAnalyticsService.getFinancialOverview(range);
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Financial overview retrieved successfully",
                metrics
        ));
    }

    @GetMapping("/order-summary")
    public ResponseEntity<ApiResponse<OrderSummaryMetrics>> getOrderSummary(
            @RequestHeader(USER_ROLES_HEADER) String roles) {

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER");

        log.info("[ANALYTICS-CONTROLLER] Order summary requested");

        OrderSummaryMetrics metrics = reportAnalyticsService.getOrderSummary();

        return ResponseEntity.ok(ApiResponse.success(
                "Order summary retrieved successfully",
                metrics
        ));
    }
}
