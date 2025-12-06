package com.sims.simscoreservice.email.dto;

import com.sims.simscoreservice.inventory.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Low Stock Alert DTO
 * Contains data for a single low stock item
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LowStockAlertDto {
    private String sku;
    private String productId;
    private String productName;
    private String category;
    private String location;
    private int currentStock;
    private int minLevel;
    private String status;

    /**
     * Create from Inventory entity
     */
    public static LowStockAlertDto from(Inventory inventory) {
        return LowStockAlertDto.builder()
                .sku(inventory.getSku())
                .productId(inventory.getProduct().getProductId())
                .productName(inventory.getProduct().getName())
                .category(inventory.getProduct().getCategory().toString())
                .location(inventory.getLocation())
                .currentStock(inventory.getCurrentStock())
                .minLevel(inventory.getMinLevel())
                .status(inventory.getStatus().toString())
                .build();
    }

    /**
     * Get stock level severity (for color coding)
     */
    public String getSeverity() {
        if (currentStock == 0) {
            return "CRITICAL"; // Red
        } else if (currentStock <= minLevel / 2) {
            return "HIGH"; // Orange
        } else {
            return "MEDIUM"; // Yellow
        }
    }
}
