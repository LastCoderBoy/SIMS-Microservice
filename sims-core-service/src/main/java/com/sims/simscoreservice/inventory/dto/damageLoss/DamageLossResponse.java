package com.sims.simscoreservice.inventory.dto.damageLoss;

import com.sims.simscoreservice.inventory.enums.LossReason;
import com.sims.simscoreservice.product.enums.ProductCategories;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Damage/Loss Response DTO
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public record DamageLossResponse(
        Integer id,
        String productName,
        ProductCategories category,
        String sku,
        Integer quantityLost,
        BigDecimal lossValue,
        LossReason reason,
        LocalDateTime lossDate,
        String recordedBy,
        LocalDateTime createdAt
) {}
