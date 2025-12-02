package com.sims.simscoreservice.stockMovement.enums;

import lombok.Getter;

/**
 * Reference Type for Stock Movement
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum StockMovementReferenceType {
    SALES_ORDER ("Sales Order Type"),
    PURCHASE_ORDER ("Purchase Order Type");

    private final String description;

    StockMovementReferenceType(String description) {
        this.description = description;
    }
}
