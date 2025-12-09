package com.sims.simscoreservice.confirmationToken.service;

import com.sims.common.utils.TokenUtils;
import com.sims.simscoreservice.confirmationToken.entity.ConfirmationToken;
import com.sims.simscoreservice.confirmationToken.enums.ConfirmationTokenStatus;
import com.sims.simscoreservice.confirmationToken.repository.ConfirmationTokenRepository;
import com.sims.simscoreservice.purchaseOrder.entity.PurchaseOrder;
import com.sims.simscoreservice.purchaseOrder.enums.PurchaseOrderStatus;
import com.sims.simscoreservice.purchaseOrder.repository.PurchaseOrderRepository;
import com.sims.simscoreservice.shared.util.GlobalServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Confirmation Token Service
 * Manages confirmation tokens for purchase order email confirmations
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmationTokenService {

    private final Clock clock;
    private final ConfirmationTokenRepository tokenRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Create confirmation token for purchase order
     */
    @Transactional
    public ConfirmationToken createConfirmationToken(PurchaseOrder order) {
        String token = TokenUtils.generateSecureToken();
        LocalDateTime now = GlobalServiceHelper.now(clock);

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                now,
                ConfirmationTokenStatus.PENDING,
                now.plusDays(1),  // Expires in 24 hours
                order
        );

        ConfirmationToken savedToken = tokenRepository.save(confirmationToken);

        log.info("[TOKEN-SERVICE] Created confirmation token for PO: {}", order.getPoNumber());

        return savedToken;
    }

    /**
     * Validate confirmation token
     * Returns null if token is invalid, expired, or already used
     */
    @Nullable
    @Transactional(readOnly = true)
    public ConfirmationToken validateConfirmationToken(String token) {
        ConfirmationToken confirmationToken = getConfirmationToken(token);

        if (confirmationToken == null) {
            log.warn("[TOKEN-SERVICE] Token not found: {}", token);
            return null;
        }

        // Check if already used
        if (confirmationToken.isUsed()) {
            log.warn("[TOKEN-SERVICE] Token already used: {}", token);
            return null;
        }

        // Check if expired
        if (confirmationToken.isExpired()) {
            log.warn("[TOKEN-SERVICE] Token expired: {}", token);
            return null;
        }

        return confirmationToken;
    }

    /**
     * Update confirmation token status
     */
    @Transactional
    public void updateConfirmationToken(ConfirmationToken token, ConfirmationTokenStatus status) {
        token.setClickedAt(GlobalServiceHelper.now(clock));
        token.setStatus(status);
        tokenRepository.save(token);

        log.info("[TOKEN-SERVICE] Token updated: {} - Status: {}", token.getToken(), status);
    }

    /**
     * Expire old tokens and mark associated POs as FAILED
     */
    @Transactional
    public void expireTokens() {
        List<ConfirmationToken> expiredTokens = tokenRepository.findAllExpiredAndUnused(LocalDateTime.now());

        if (expiredTokens.isEmpty()) {
            log.info("[TOKEN-SERVICE] No expired tokens found");
            return;
        }

        for (ConfirmationToken token : expiredTokens) {
            PurchaseOrder order = token.getOrder();

            // Set PO status to FAILED
            order.setStatus(PurchaseOrderStatus.FAILED);
            order.setUpdatedBy("System - Token Expired");
            purchaseOrderRepository.save(order);

            // Delete expired token
            tokenRepository.delete(token);

            log.info("[TOKEN-SERVICE] Expired token deleted for PO: {}", order.getPoNumber());
        }

        log.info("[TOKEN-SERVICE] Expired {} confirmation tokens", expiredTokens.size());
    }

    /**
     * Get confirmation status for display
     */
    @Transactional(readOnly = true)
    public Map<String, String> getConfirmationStatus(String token) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token).orElse(null);

        if (confirmationToken == null) {
            return buildStatusMap("Invalid token.", "alert-danger");
        }

        return switch (confirmationToken.getStatus()) {
            case CONFIRMED -> buildStatusMap(
                    "Order " + confirmationToken.getOrder().getPoNumber() + " confirmed successfully.",
                    "alert-success"
            );
            case CANCELLED -> buildStatusMap(
                    "Order " + confirmationToken.getOrder().getPoNumber() + " cancelled successfully.",
                    "alert-warning"
            );
            case PENDING -> {
                if (confirmationToken.isExpired()) {
                    yield buildStatusMap("Token expired.", "alert-danger");
                }
                yield buildStatusMap("Token pending confirmation.", "alert-info");
            }
        };
    }

    /**
     * Get confirmation token by token string
     */
    @Nullable
    private ConfirmationToken getConfirmationToken(String token) {
        return tokenRepository.findByToken(token).orElse(null);
    }

    /**
     * Build status map for display
     */
    private Map<String, String> buildStatusMap(String message, String alertClass) {
        Map<String, String> data = new HashMap<>();
        data.put("message", message);
        data.put("alertClass", alertClass);
        return data;
    }
}
