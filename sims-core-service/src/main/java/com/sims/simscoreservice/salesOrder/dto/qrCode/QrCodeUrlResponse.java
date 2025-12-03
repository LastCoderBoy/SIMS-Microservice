package com.sims.simscoreservice.salesOrder.dto.qrCode;

import java.time.LocalDateTime;

/**
 * QR Code URL Response DTO
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public record QrCodeUrlResponse(
        String qrImageUrl,
        String orderReference,
        LocalDateTime expiresAt
) {}
