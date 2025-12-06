package com.sims.simscoreservice.email.lowStockAlert;


import com.sims.simscoreservice.inventory.entity.Inventory;
import com.sims.simscoreservice.inventory.queryService.InventoryQueryService;
import com.sims.simscoreservice.email.EmailService;
import com.sims.simscoreservice.email.dto.LowStockAlertDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.sims.common.constants.AppConstants.DEFAULT_SORT_BY;
import static com.sims.common.constants.AppConstants.DEFAULT_SORT_DIRECTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class LowStockScheduler {

    private final InventoryQueryService inventoryQueryService;
    private final EmailService emailService;


    //    @Scheduled(cron = "*/30 * * * * ?")
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyLowStockAlert() {
        log.info("[LOW-STOCK-SCHEDULER] Starting daily low stock check.. .");

        try {
            // Get all low stock products
            List<Inventory> lowStockProducts = inventoryQueryService.getAllLowStockProducts(
                    DEFAULT_SORT_BY,
                    DEFAULT_SORT_DIRECTION
            );

            // If no low stock products, skip email
            if (lowStockProducts.isEmpty()) {
                log. info("[LOW-STOCK-SCHEDULER] No low stock products found.  Skipping alert.");
                return;
            }

            // Convert to DTOs
            List<LowStockAlertDto> alertDtos = lowStockProducts.stream()
                    .map(LowStockAlertDto::from)
                    .collect(Collectors.toList());

            // Send email
            emailService.sendLowStockAlert(alertDtos);

            log.info("[LOW-STOCK-SCHEDULER] Daily low stock alert sent for {} products", alertDtos.size());

        } catch (Exception e) {
            log.error("[LOW-STOCK-SCHEDULER] Error sending daily low stock alert: {}", e.getMessage(), e);
            // No need to throw - we don't want to stop the scheduler
        }
    }

    /**
     * Manual trigger for testing (optional)
     * Can be called via controller for testing
     */
    public void triggerManualAlert() {
        log.info("[LOW-STOCK-SCHEDULER] Manual alert triggered");
        sendDailyLowStockAlert();
    }

}
