package com.sims.simscoreservice.inventory.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inventory Request DTO
 * Used for updating inventory levels
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {

    @Min(value = 0, message = "Current stock cannot be negative")
    private Integer currentStock;

    @Min(value = 0, message = "Minimum level cannot be negative")
    private Integer minLevel;
}
