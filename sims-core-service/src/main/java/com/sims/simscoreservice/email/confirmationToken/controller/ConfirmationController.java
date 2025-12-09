package com.sims.simscoreservice.email.confirmationToken.controller;

import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.email.confirmationToken.dto.ConfirmationPoRequest;
import com.sims.simscoreservice.email.confirmationToken.service.ConfirmationTokenService;
import com.sims.simscoreservice.email.confirmationToken.service.PurchaseOrderConfirmationService;
import com.sims.simscoreservice.purchaseOrder.dto.PurchaseOrderDetailsView;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.sims.common.constants.AppConstants.BASE_EMAIL_PATH;

/**
 * Confirmation Controller
 * Handles email confirmation links for purchase orders
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping(BASE_EMAIL_PATH + "/purchase-order")
@RequiredArgsConstructor
@Slf4j
public class ConfirmationController {

    private final PurchaseOrderConfirmationService purchaseOrderConfirmationService;
    private final ConfirmationTokenService confirmationTokenService;

    /**
     * Redirect to confirmation form (GET - from email link)
     */
    @GetMapping("/confirm")
    public void showConfirmationForm(
            @RequestParam String token,
            HttpServletResponse response) throws IOException {

        if(confirmationTokenService.validateConfirmationToken(token) == null){
            log.error("[CONFIRMATION-CONTROLLER] Invalid token for confirmation");

            response.sendRedirect("/email/forms/token-error.html");
            return;
        }

        log.info("[CONFIRMATION-CONTROLLER] Redirecting to confirm form for token: {}...",
                token.substring(0, Math.min(10, token.length())));

        // Redirect to static HTML with token as query param
        response.sendRedirect("/email/forms/confirm-purchase-order.html?token=" + token);
    }

    /**
     * Confirm purchase order (from supplier email)
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<String>> confirmPurchaseOrder(
            @RequestParam String token,
            @Valid @RequestBody ConfirmationPoRequest request) {

        log.info("[CONFIRMATION-CONTROLLER] Confirm PO with token: {}...",
                token.substring(0, Math.min(10, token.length())));

        ApiResponse<String> response =
                purchaseOrderConfirmationService.confirmPurchaseOrder(token, request.getExpectedArrivalDate());

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Redirect to cancellation form (GET - from email link)
     */
    @GetMapping("/cancel")
    public void showCancellationForm(
            @RequestParam String token,
            HttpServletResponse response) throws IOException {

        if(confirmationTokenService.validateConfirmationToken(token) == null){
            log.error("[CONFIRMATION-CONTROLLER] Invalid token for cancellation");

            response.sendRedirect("/email/forms/token-error.html");
            return;
        }

        log.info("[CONFIRMATION-CONTROLLER] Redirecting to cancel form for token: {}.. .",
                token.substring(0, Math.min(10, token.length())));

        // Redirect to static HTML with token as query param
        response.sendRedirect("/email/forms/cancel-purchase-order.html?token=" + token);
    }

    /**
     * Cancel purchase order (POST - from form submission)
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> cancelPurchaseOrder(@RequestParam String token) {

        log.info("[CONFIRMATION-CONTROLLER] Cancel PO with token: {}...",
                token.substring(0, Math.min(10, token.length())));

        ApiResponse<String> response = purchaseOrderConfirmationService.cancelPurchaseOrder(token);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus. OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Get PO details by token (for displaying in confirmation form)
     * Public endpoint - no auth needed (token itself is the auth)
     */
    @GetMapping("/details")
    public ResponseEntity<PurchaseOrderDetailsView> getPurchaseOrderDetails(@RequestParam String token) {
        log.debug("[CONFIRMATION-CONTROLLER] Get PO details for token: {}.. .",
                token.substring(0, Math.min(10, token.length())));

        try {
            PurchaseOrderDetailsView details = purchaseOrderConfirmationService.getPurchaseOrderDetailsByToken(token);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("[CONFIRMATION-CONTROLLER] Error getting PO details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
