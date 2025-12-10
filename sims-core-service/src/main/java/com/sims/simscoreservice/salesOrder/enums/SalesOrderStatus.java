package com.sims.simscoreservice.salesOrder.enums;

import lombok.Getter;

/**
 * Sales Order Status Enum
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum SalesOrderStatus {
    PENDING("Order created, awaiting confirmation"),
    PARTIALLY_APPROVED("Order partially confirmed"),
    PARTIALLY_DELIVERED("Order partially shipped"),
    APPROVED("Order confirmed, ready for delivery"),
    DELIVERY_IN_PROCESS("Order is being delivered"),
    DELIVERED("Order is delivered to customer"),
    CANCELLED("Order is cancelled"),
    COMPLETED("Order is completed");

    private final String description;

    SalesOrderStatus(String description) {
        this.description = description;
    }
}
