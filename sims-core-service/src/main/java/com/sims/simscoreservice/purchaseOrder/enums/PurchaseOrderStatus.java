package com.sims.simscoreservice.purchaseOrder.enums;

import lombok.Getter;

/**
 * Purchase Order Status Enum
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum PurchaseOrderStatus {
    AWAITING_APPROVAL("Awaiting supplier confirmation"),
    DELIVERY_IN_PROCESS("Order placed, awaiting arrival"),
    PARTIALLY_RECEIVED("Some items received, more are expected"),
    RECEIVED("All ordered items have been received"),
    CANCELLED("Order was cancelled before full receipt"),
    FAILED("Delivery failed or was rejected");

    private final String description;

    PurchaseOrderStatus(String description) {
        this.description = description;
    }
}
