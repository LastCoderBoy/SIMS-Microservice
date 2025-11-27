package com.sims.simscoreservice.inventory.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Process Sales Order Request
 * Used for bulk stock out operations
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessSalesOrderRequestDto {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Item quantities are required")
    private Map<String, Integer> itemQuantities; // ProductID: Shipped quantity
}

