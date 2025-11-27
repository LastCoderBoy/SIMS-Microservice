package com.sims.simscoreservice.inventory.enums;

import lombok.Getter;

/**
 * Inventory Status
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum InventoryStatus {
    INCOMING("Stock is incoming from supplier"),
    IN_STOCK("Product is available in stock"),
    LOW_STOCK("Stock level is below minimum"),
    INVALID("Product is not for sale (restricted, archived, discontinued)");

    private final String description;

    InventoryStatus(String description) {
        this. description = description;
    }
}
