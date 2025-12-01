package com.sims.simscoreservice.inventory.dto.damageLoss;

import com.sims.simscoreservice.inventory.enums.LossReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Damage/Loss Request DTO
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public record DamageLossRequest(
        @NotBlank(message = "SKU is required")
        String sku,

        @NotNull(message = "Quantity lost is required")
        @Min(value = 1, message = "Quantity lost must be at least 1")
        Integer quantityLost,

        @NotNull(message = "Loss reason is required")
        LossReason reason,

        LocalDateTime lossDate  // Optional, defaults to now
) {}
