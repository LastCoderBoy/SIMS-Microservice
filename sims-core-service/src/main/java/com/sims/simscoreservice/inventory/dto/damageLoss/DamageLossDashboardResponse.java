package com.sims.simscoreservice.inventory.dto.damageLoss;


import com.sims.common.models.PaginatedResponse;

import java.math.BigDecimal;

/**
 * Damage/Loss Dashboard Response
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public record DamageLossDashboardResponse(
        Long totalReports,
        Long totalItems,
        BigDecimal totalLossValue,
        PaginatedResponse<DamageLossResponse> reports
) {}
