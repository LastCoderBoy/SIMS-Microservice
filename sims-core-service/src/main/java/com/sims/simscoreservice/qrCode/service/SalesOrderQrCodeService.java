package com.sims.simscoreservice.qrCode.service;

import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.qrCode.dto.QrCodeUrlResponse;
import com.sims.simscoreservice.qrCode.entity.SalesOrderQRCode;
import com.sims.simscoreservice.salesOrder.dto.DetailedSalesOrderView;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Sales Order QR Code Service
 * Handles QR code generation, verification, and management
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public interface SalesOrderQrCodeService {

    /**
     * Generate QR code and upload to S3
     *
     * @param orderReference Sales order reference
     * @return Generated QR code entity
     */
    SalesOrderQRCode generateAndLinkQrCode(String orderReference);

    /**
     * Get presigned URL for QR code image
     *
     * @param salesOrderId Sales order ID
     * @return Presigned URL response
     */
    QrCodeUrlResponse getPresignedQrCodeUrl(Long salesOrderId);

    /**
     * Verify QR code by token
     *
     * @param qrToken QR token from scan
     * @param userId User scanning the QR code
     * @param request HTTP request for IP/user agent tracking
     * @return Detailed sales order view
     */
    DetailedSalesOrderView verifyQrCode(String qrToken, String userId, HttpServletRequest request);

    /**
     * Update order status via QR code scan
     *
     * @param qrToken QR token
     * @param userId User updating the status
     * @param newStatus New status to set
     * @param request HTTP request for tracking
     * @return API response
     */
    ApiResponse<String> updateOrderStatus(
            String qrToken, String userId, SalesOrderStatus newStatus, HttpServletRequest request);

    /**
     * Delete QR code from S3 (for rollback)
     *
     * @param s3Key S3 object key
     */
    void deleteQrCodeFromS3(String s3Key);
}
