package com.sims.simscoreservice.salesOrder.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Process Sales Order Request DTO
 * Used for bulk stock out operations (fulfilling orders)
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

    @NotEmpty(message = "Item quantities cannot be empty")
    private Map<String, Integer> itemQuantities;  // ProductID: Shipped quantity
}
