package com.sims.simscoreservice.analytics.service;

import com.sims.simscoreservice.analytics.dto.InventoryReportMetrics;

import java.math.BigDecimal;

/**
 * Inventory Health Service
 * Analyzes inventory health and stock metrics
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface InventoryHealthService {

    /**
     * Get complete inventory health metrics
     */
    InventoryReportMetrics getInventoryHealth();

    /**
     * Calculate total inventory stock value at retail price
     */
    BigDecimal calculateInventoryStockValueAtRetail();
}
