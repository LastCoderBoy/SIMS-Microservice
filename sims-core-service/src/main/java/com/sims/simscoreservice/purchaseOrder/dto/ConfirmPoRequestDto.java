package com.sims.simscoreservice.purchaseOrder.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Confirm Purchase Order Request DTO
 * Used by supplier to confirm order via email
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmPoRequestDto {

    @NotNull(message = "Expected arrival date is required")
    @FutureOrPresent(message = "Arrival date must be today or in the future")
    private LocalDate expectedArrivalDate;
}
