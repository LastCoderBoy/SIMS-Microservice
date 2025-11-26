package com.sims.simscoreservice.product.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Product Status
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum ProductStatus {
    ACTIVE(true, "Product is ready to sell"),
    ON_ORDER(true, "Product order confirmed by supplier"),
    PLANNING(false, "Product is being considered to sell"),

    // Invalid statuses
    DISCONTINUED(false, "Product is stopped but present in inventory"),
    ARCHIVED(false, "Product has been sold before"),
    RESTRICTED(false, "Product is restricted");

    private final boolean active;
    private final String description;

    ProductStatus(boolean active, String description) {
        this.active = active;
        this.description = description;
    }

    /**
     * Get all active statuses
     */
    public static List<ProductStatus> getActiveStatuses() {
        return Arrays.stream(values())
                .filter(ProductStatus::isActive)
                .toList();
    }

    /**
     * Get all inactive statuses
     */
    public static List<ProductStatus> getInactiveStatuses() {
        return Arrays.stream(values())
                .filter(status -> !status.isActive())
                .toList();
    }
}