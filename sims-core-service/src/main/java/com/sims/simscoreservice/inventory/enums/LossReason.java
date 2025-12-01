package com.sims.simscoreservice.inventory.enums;

import lombok.Getter;

/**
 * Loss Reason Enum
 * Reasons for damage or loss of inventory
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum LossReason {
    LOST("Item was lost or misplaced"),
    DAMAGED("Item was damaged beyond repair"),
    SUPPLIER_FAULT("Received damaged from supplier"),
    TRANSPORT_DAMAGE("Damaged during transportation");

    private final String description;

    LossReason(String description) {
        this.description = description;
    }
}
