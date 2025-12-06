package com.sims.simscoreservice.admin.controller;

import com.sims.simscoreservice.inventory.service.lowStockAlert.LowStockScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sims.common.constants.AppConstants.BASE_ADMIN_PATH;

/**
 * Scheduler Test Controller
 * For manually triggering scheduled tasks (development/testing only)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping( BASE_ADMIN_PATH + "/scheduler")
@RequiredArgsConstructor
@Slf4j
public class SchedulerTestController {

    private final LowStockScheduler lowStockScheduler;

    /**
     * Manually trigger low stock alert (for testing)
     */
    @PostMapping("/low-stock-alert")
    public ResponseEntity<String> triggerLowStockAlert() {
        log.info("[SCHEDULER-TEST] Manual low stock alert triggered");

        lowStockScheduler.triggerManualAlert();

        return ResponseEntity.ok("Low stock alert triggered successfully");
    }
}