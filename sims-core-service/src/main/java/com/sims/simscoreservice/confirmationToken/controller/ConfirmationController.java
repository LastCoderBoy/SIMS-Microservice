package com.sims.simscoreservice.confirmationToken.controller;

import com.sims.common.models.ApiResponse;
import com.sims.simscoreservice.confirmationToken.dto.ConfirmationPoRequest;
import com.sims.simscoreservice.confirmationToken.service.PurchaseOrderConfirmationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final PurchaseOrderConfirmationService confirmationService;

    /**
     * Confirm purchase order (from supplier email)
     */
    @PostMapping("/confirm")
    public ResponseEntity<String> confirmPurchaseOrder(
            @RequestParam String token,
            @Valid @RequestBody ConfirmationPoRequest request) {

        log.info("[CONFIRMATION-CONTROLLER] Confirm PO with token: {}", token);

        ApiResponse<String> response = confirmationService.confirmPurchaseOrder(token, request.getExpectedArrivalDate());

        String htmlResponse = buildConfirmationPage(
                response.getMessage(),
                response.isSuccess() ? "alert-success" : "alert-danger"
        );

        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(htmlResponse);
    }

    /**
     * Cancel purchase order (from supplier email)
     */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPurchaseOrder(@RequestParam String token) {

        log.info("[CONFIRMATION-CONTROLLER] Cancel PO with token: {}", token);

        ApiResponse<String> response = confirmationService.cancelPurchaseOrder(token);

        String htmlResponse = buildConfirmationPage(
                response.getMessage(),
                response.isSuccess() ? "alert-success" : "alert-danger"
        );

        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(htmlResponse);
    }

    /**
     * Build HTML confirmation page
     */
    private String buildConfirmationPage(String message, String alertClass) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Purchase Order Confirmation</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 40px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                        .alert { padding: 20px; margin: 20px 0; border-radius: 5px; }
                        .alert-success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
                        .alert-danger { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
                        h2 { color: #0056b3; margin-bottom: 20px; }
                        p { font-size: 16px; line-height: 1.6; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>SIMS Inventory System</h2>
                        <div class="alert %s">
                            <p>%s</p>
                        </div>
                        <p>You can close this window.</p>
                    </div>
                </body>
                </html>
                """.formatted(alertClass, message);
    }
}
