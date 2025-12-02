package com.sims.simscoreservice.stockMovement.enums;

import lombok.Getter;

/**
 * Stock Movement Type Enum used for tracking stock movements
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum StockMovementType {
    IN("Incoming Stock Type"),
    OUT ("Outgoing Stock Type");

    private final String description;

    StockMovementType(String description) {
        this.description = description;
    }
}
