package com.sims.simscoreservice.email.confirmationToken.dto;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Confirm Purchase Order Request DTO
 * Used by supplier to confirm order and provide expected arrival date
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmationPoRequest {

    @NotNull(message = "Expected arrival date is required")
    @FutureOrPresent(message = "Arrival date must be today or in the future")
    private LocalDate expectedArrivalDate;
}
