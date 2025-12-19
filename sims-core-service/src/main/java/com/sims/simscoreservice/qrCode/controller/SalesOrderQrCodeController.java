package com.sims.simscoreservice.qrCode.controller;

import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.qrCode.dto.QrCodeUrlResponse;
import com.sims.simscoreservice.qrCode.service.SalesOrderQrCodeService;
import com.sims.simscoreservice.salesOrder.dto.DetailedSalesOrderView;
import com.sims.simscoreservice.salesOrder.enums.SalesOrderStatus;
import com.sims.simscoreservice.shared.util.RoleValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sims.common.constants.AppConstants.*;

/**
 * Sales Order QR Code Controller
 * Handles QR code operations
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(API_VERSION_V1 + "/sales-orders/qrcode")
@RequiredArgsConstructor
@Slf4j
public class SalesOrderQrCodeController {

    private final SalesOrderQrCodeService salesOrderQrCodeService;
    private final RoleValidator roleValidator;

    /**
     * Get presigned URL for QR code image
     */
    @GetMapping("/{salesOrderId}/view")
    public ResponseEntity<ApiResponse<QrCodeUrlResponse>> viewQrCode(@PathVariable Long salesOrderId) {
        log.info("[SO-QR-CONTROLLER] Getting QR code URL for sales order: {}", salesOrderId);

        QrCodeUrlResponse qrCodeUrlResponse = salesOrderQrCodeService.getPresignedQrCodeUrl(salesOrderId);

        return ResponseEntity.ok(ApiResponse.success(
                "QR Code URL generated successfully",
                qrCodeUrlResponse
        ));
    }

    /**
     * Verify QR code by scanning
     */
    @GetMapping("/{qrToken}/verify")
    public ResponseEntity<ApiResponse<DetailedSalesOrderView>> verifyQrCode(
            @PathVariable @NotBlank(message = "QR token is required") String qrToken,
            @RequestHeader(value = USER_ID_HEADER, required = false) String userId,
            HttpServletRequest request) {

        log.info("[SO-QR-CONTROLLER] Verifying QR code:  {} by user: {}",
                qrToken.substring(0, Math.min(10, qrToken.length())), userId);

        if (userId == null || userId.trim().isEmpty()) {
            userId = "GUEST"; // For anonymous scans
        }

        DetailedSalesOrderView orderView = salesOrderQrCodeService.verifyQrCode(qrToken, userId, request);

        return ResponseEntity.ok(ApiResponse.success(
                "QR code verified successfully",
                orderView
        ));
    }

    /**
     * Update order status via QR code
     * Used by couriers/managers to update delivery status
     */
    @PatchMapping("/{qrToken}")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
            @PathVariable String qrToken,
            @Valid @RequestParam SalesOrderStatus status,
            @RequestHeader(USER_ID_HEADER) String userId,
            @RequestHeader(USER_ROLES_HEADER) String roles,
            HttpServletRequest request) {

        log.info("[SO-QR-CONTROLLER] Updating order status via QR:  {} to {} by user: {}",
                qrToken.substring(0, Math.min(10, qrToken.length())),
                status,
                userId);

        // Check authorization
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_COURIER"); // might throw Forbidden Exception

        ApiResponse<String> response = salesOrderQrCodeService.updateOrderStatus(
                qrToken, userId, status, request);

        return ResponseEntity.ok(response);
    }
}
