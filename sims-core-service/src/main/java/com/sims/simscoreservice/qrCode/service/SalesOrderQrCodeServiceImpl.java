package com.sims.simscoreservice.qrCode.service;

import com.google.zxing.WriterException;
import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.common.exceptions.ServiceException;
import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.ApiResponse;
import com.sims.common.utils.TokenUtils;
import com.sims.simscoreservice.qrCode.dto.QrCodeUrlResponse;
import com.sims.simscoreservice.qrCode.entity.SalesOrderQRCode;
import com.sims.simscoreservice.qrCode.repository.SalesOrderQrCodeRepository;
import com.sims.simscoreservice.qrCode.util.QrCodeUtil;
import com.sims.simscoreservice.salesOrder.dto.DetailedSalesOrderView;
import com.sims.simscoreservice.salesOrder.entity.SalesOrder;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.salesOrder.repository.SalesOrderRepository;
import com.sims.simscoreservice.shared.s3.service.S3Service;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import com.sims.simscoreservice.shared.util.HttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;

import static com.sims.common.constants.AppConstants.*;

/**
 * Sales Order QR Code Service Implementation
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderQrCodeServiceImpl implements SalesOrderQrCodeService {

    private final Clock clock;

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderQrCodeRepository qrCodeRepository;
    private final S3Service s3Service;
    private final QrCodeUtil qrCodeUtil;

    @Override
    @Transactional
    public SalesOrderQRCode generateAndLinkQrCode(String orderReference) {
        try {
            log.info("[QR-SERVICE] Generating QR code for order: {}", orderReference);

            // Generate unique token
            String qrToken = TokenUtils.generateSecureToken();

            // Build QR code data (URL that will be scanned) & Generate QR code image
            String qrCodeData = BASE_URL + API_VERSION_V1 + "/sales-orders/qrcode/" + qrToken + "/verify";
            byte[] qrCodeImage = qrCodeUtil.generateQrCodeImage(qrCodeData, QR_CODE_WIDTH, QR_CODE_HEIGHT);

            // Upload to S3
            String s3Key = QR_CODE_S3_PREFIX + orderReference + ".png";
            s3Service.uploadFile(s3Key, qrCodeImage, "image/png");

            // Create QR code entity
            SalesOrderQRCode qrCode = new SalesOrderQRCode();
            qrCode.setQrToken(qrToken);
            qrCode.setQrCodeS3Key(s3Key);
            qrCode.setGeneratedAt(LocalDateTime.now());

            log.info("[QR-SERVICE] QR code generated successfully for order: {}", orderReference);
            return qrCode; // the Cascade setting will automatically save the SalesOrder entity

        } catch (WriterException e) {
            log.error("[QR-SERVICE] Failed to generate QR code image: {}", e.getMessage());
            throw new ServiceException("Failed to generate QR code image", e);
        } catch (IOException e) {
            log.error("[QR-SERVICE] Failed to create QR code: {}", e.getMessage());
            throw new ServiceException("Failed to create QR code", e);
        } catch (Exception e) {
            log.error("[QR-SERVICE] Unexpected error generating QR code: {}", e.getMessage(), e);
            throw new ServiceException("Failed to generate QR code", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public QrCodeUrlResponse getPresignedQrCodeUrl(Long salesOrderId) {
        try {
            log.info("[QR-SERVICE] Getting presigned URL for sales order: {}", salesOrderId);

            SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with ID: " + salesOrderId));

            // Check if QR code exists
            if (salesOrder.getQrCode() == null) {
                throw new ResourceNotFoundException("QR code not found for order: " + salesOrder.getOrderReference());
            }

            SalesOrderQRCode qrCode = salesOrder.getQrCode();

            // Generate presigned URL
            String presignedUrl = s3Service.generatePresignedUrl(qrCode.getQrCodeS3Key(), PRESIGNED_URL_DURATION);

            log.info("[QR-SERVICE] Generated presigned URL for order: {}", salesOrder.getOrderReference());

            return QrCodeUrlResponse.builder()
                    .qrCodeUrl(presignedUrl)
                    .qrToken(qrCode.getQrToken())
                    .orderReference(salesOrder.getOrderReference())
                    .expiresIn(PRESIGNED_URL_DURATION.toMinutes())
                    .build();

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[QR-SERVICE] Error generating presigned URL: {}", e.getMessage(), e);
            throw new ServiceException("Failed to generate QR code URL", e);
        }
    }

    @Override
    @Transactional
    public DetailedSalesOrderView verifyQrCode(String qrToken, String userId, HttpServletRequest request) {
        try {
            log.info("[QR-SERVICE] Verifying QR code:  {} by user: {}", qrToken, userId);

            SalesOrderQRCode qrCode = findByQrToken(qrToken);

            SalesOrder salesOrder = qrCode.getSalesOrder();
            if (salesOrder == null) {
                throw new ResourceNotFoundException("No sales order linked to this QR code");
            }

            // Update scan tracking
            logScanner(qrCode, userId, request);

            qrCodeRepository.save(qrCode);

            log.info("[QR-SERVICE] QR code verified for order: {}", salesOrder.getOrderReference());

            return new DetailedSalesOrderView(salesOrder);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[QR-SERVICE] Error verifying QR code: {}", e.getMessage(), e);
            throw new ServiceException("Failed to verify QR code", e);
        }
    }

    // ORDER STATUS UPDATE VIA QR
    @Override
    @Transactional
    public ApiResponse<String> updateOrderStatus(
            String qrToken, String userId, SalesOrderStatus newStatus, HttpServletRequest request) {

        try {
            log.info("[QR-SERVICE] Updating order status via QR:  {} to {} by user: {}",
                    qrToken, newStatus, userId);

            SalesOrderQRCode qrCode = findByQrToken(qrToken);

            // Get associated sales order
            SalesOrder salesOrder = qrCode.getSalesOrder();
            if (salesOrder == null) {
                throw new ResourceNotFoundException("No sales order linked to this QR code");
            }

            // Validate status transition
            validateStatusTransition(salesOrder.getStatus(), newStatus);

            // Update order status
            SalesOrderStatus oldStatus = salesOrder.getStatus();
            salesOrder.setStatus(newStatus);
            salesOrder.setUpdatedBy(userId);

            // Set delivery date if status is DELIVERED
            if (newStatus == SalesOrderStatus.DELIVERED) {
                salesOrder.setDeliveryDate(GlobalServiceHelper.now(clock));
                log.debug("[QR-SERVICE] Set delivery date for order: {}", salesOrder.getOrderReference());
            }

            // Update scan tracking
            logScanner(qrCode, userId, request);

            qrCodeRepository.save(qrCode);

            log.info("[QR-SERVICE] Order {} status updated from {} to {}",
                    salesOrder.getOrderReference(), oldStatus, newStatus);

            return ApiResponse.success(
                    String.format("Order %s status updated to %s", salesOrder.getOrderReference(), newStatus));

        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[QR-SERVICE] Error updating order status: {}", e.getMessage(), e);
            throw new ServiceException("Failed to update order status", e);
        }
    }

    // ========================================
    // S3 MANAGEMENT
    // ========================================

    @Override
    public void deleteQrCodeFromS3(String s3Key) {
        try {
            log.info("[QR-SERVICE] Deleting QR code from S3: {}", s3Key);
            s3Service.deleteFile(s3Key);
            log.info("[QR-SERVICE] Successfully deleted QR code from S3: {}", s3Key);
        } catch (Exception e) {
            log.error("[QR-SERVICE] Failed to delete QR code from S3 {}: {}", s3Key, e.getMessage());
            throw new ServiceException("Failed to delete QR code from S3", e);
        }
    }

    /**
     * Update scan tracking information
     */
    private void logScanner(SalesOrderQRCode qrCode, String userId, HttpServletRequest request) {
        try {
            qrCode.setLastScannedAt(GlobalServiceHelper.now(clock));
            qrCode.setScannedBy(userId);
            qrCode.setIpAddress(HttpRequestUtil.extractIpAddress(request));
            qrCode.setUserAgent(HttpRequestUtil.extractUserAgent(request));

            log.debug("[QR-SERVICE] Updated scan tracking for QR token: {}", qrCode.getQrToken());

        } catch (Exception e) {
            log.error("SO-QR: logScanner() Error logging scanner details - {}", e.getMessage());
        }
    }

    private void validateStatusTransition(SalesOrderStatus currentStatus, SalesOrderStatus newStatus) {
        // Define valid transitions
        boolean isValid = switch (currentStatus) {

            case APPROVED, DELIVERY_IN_PROCESS ->
                    newStatus == SalesOrderStatus.DELIVERY_IN_PROCESS ||
                    newStatus == SalesOrderStatus.DELIVERED ||
                    newStatus == SalesOrderStatus.COMPLETED;

            default -> false;
        };

        if (!isValid) {
            log.warn("[QR-SERVICE] Invalid status transition:  {} -> {}", currentStatus, newStatus);
            throw new ValidationException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }
    }

    private SalesOrderQRCode findByQrToken(String qrToken){
        return qrCodeRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with token: " + qrToken));
    }
}