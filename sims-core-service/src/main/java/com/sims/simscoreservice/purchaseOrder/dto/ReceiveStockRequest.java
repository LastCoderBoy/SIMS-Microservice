package com.sims.simscoreservice.purchaseOrder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Receive Stock Request
 * Used when receiving Purchase Order
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveStockRequest {

    @NotNull(message = "Received quantity is required")
    @Min(value = 0, message = "Received quantity must be at least 0")
    private Integer receivedQuantity;

    private LocalDate actualArrivalDate;
}

