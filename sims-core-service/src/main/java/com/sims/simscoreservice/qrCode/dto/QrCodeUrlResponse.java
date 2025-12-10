package com.sims.simscoreservice.qrCode.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * QR Code URL Response
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QrCodeUrlResponse {
    private String qrCodeUrl;        // Presigned S3 URL
    private String qrToken;          // QR token for verification
    private String orderReference;   // Sales order reference
    private Long expiresIn;          // URL expiration in minutes
}
