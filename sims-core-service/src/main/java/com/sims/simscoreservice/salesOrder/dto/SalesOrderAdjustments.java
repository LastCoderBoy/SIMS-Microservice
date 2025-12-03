package com.sims.simscoreservice.salesOrder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sales Order Adjustments DTO
 * Tracks stock adjustments (reservations/releases)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderAdjustments {
    private String productId;
    private int quantity;
    private boolean wasReserved; // true if reserved, false if released
}
