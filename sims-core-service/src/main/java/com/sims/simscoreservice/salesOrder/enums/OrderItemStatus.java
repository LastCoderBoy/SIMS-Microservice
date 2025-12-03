package com.sims.simscoreservice.salesOrder.enums;

import lombok.Getter;

/**
 * Order Item Status Enum
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum OrderItemStatus {
    PENDING("Awaiting processing"),
    PARTIALLY_APPROVED("Partially approved for shipment"),
    APPROVED("Approved for shipment"),
    CANCELLED("Item cancelled");

    private final String description;

    OrderItemStatus(String description) {
        this.description = description;
    }
}
