package com.sims.simscoreservice.analytics.service.impl;

import com.sims.common.exceptions.DatabaseException;
import com.sims.common.exceptions.ServiceException;
import com.sims.simscoreservice.analytics.dto.InventoryReportMetrics;
import com.sims.simscoreservice.analytics.service.InventoryHealthService;
import com.sims.simscoreservice.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Inventory Health Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryHealthServiceImpl implements InventoryHealthService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public InventoryReportMetrics getInventoryHealth() {
        try {
            log.info("[ANALYTICS-INV] Calculating inventory health metrics");

            InventoryReportMetrics metrics = inventoryRepository.getInventoryReportMetrics();

            log.info("[ANALYTICS-INV] Inventory health:  {} - Score: {}",
                    metrics.getHealthStatus(),
                    String.format("%.2f%%", metrics.getHealthScore()));

            return metrics;

        } catch (DataAccessException e) {
            log.error("[ANALYTICS-INV] Database error calculating inventory health: {}", e.getMessage());
            throw new DatabaseException("Failed to calculate inventory health", e);
        } catch (Exception e) {
            log.error("[ANALYTICS-INV] Unexpected error:  {}", e.getMessage(), e);
            throw new ServiceException("Failed to calculate inventory health", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateInventoryStockValueAtRetail() {
        try {
            log.debug("[ANALYTICS-INV] Calculating inventory stock value at retail");

            BigDecimal stockValue = inventoryRepository.getInventoryStockValueAtRetail();

            log.debug("[ANALYTICS-INV] Total inventory stock value:  ${}", stockValue);

            return stockValue;

        } catch (DataAccessException e) {
            log.error("[ANALYTICS-INV] Database error calculating stock value: {}", e.getMessage());
            throw new DatabaseException("Failed to calculate inventory stock value", e);
        } catch (Exception e) {
            log.error("[ANALYTICS-INV] Unexpected error: {}", e.getMessage(), e);
            throw new ServiceException("Failed to calculate inventory stock value", e);
        }
    }
}
