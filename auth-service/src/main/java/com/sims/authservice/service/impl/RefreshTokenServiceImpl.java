package com.sims.authservice.service.impl;

import com.sims.authservice.entity.RefreshToken;
import com.sims.authservice.entity.Users;
import com.sims.authservice.exception.TokenRefreshException;
import com.sims.authservice.repository.RefreshTokenRepository;
import com.sims.authservice.repository.UserRepository;
import com.sims.authservice.service.RefreshTokenService;
import com.sims.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * Refresh Token Service Implementation
 * Handles refresh token lifecycle (create, validate, rotate, revoke)
 *
 * @author LastCoderBoy
 * @since 2025-01-20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int MAX_ACTIVE_TOKENS_PER_USER = 5;

    @Value("${jwt.refresh.expiration}") // 7 days in milliseconds
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(String username, String ipAddress, String userAgent) {
        Users user = findUserByUsername(username);

        // Limit active tokens per user
        limitActiveTokens(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(generateSecureToken());
        refreshToken.setUser(user);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        log.info("[REFRESH-TOKEN] Created refresh token for user: {} from IP: {}", user.getUsername(), ipAddress);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(String token) {
        RefreshToken refreshToken = findByToken(token);

        if (refreshToken.isRevoked()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token has been revoked. Please login again.");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token has expired. Please login again.");
        }

        return refreshToken;
    }

    /**
     * Generate cryptographically secure random token
     */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Limit active tokens per user
     */
    private void limitActiveTokens(Users user) {
        List<RefreshToken> activeTokens = refreshTokenRepository
                .findActiveTokensByUser(user, Instant.now());

        if (activeTokens.size() >= MAX_ACTIVE_TOKENS_PER_USER) {
            RefreshToken oldest = activeTokens.get(0);
            oldest.setRevoked(true);
            refreshTokenRepository.save(oldest);
            log.info("[REFRESH-TOKEN] Revoked oldest token for user {} (limit reached)", user.getUsername());
        }
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("[REFRESH-TOKEN] Revoked refresh token for user: {}", rt.getUser().getUsername());
        });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Users user) {
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("[REFRESH-TOKEN] Revoked all tokens for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, String ipAddress, String userAgent) {
        if (oldToken.isRevoked()) {
            log.warn("[REFRESH-TOKEN] Attempted to rotate already revoked token - possible token theft!");
            throw new TokenRefreshException("Token has already been used");
        }

        // Mark as revoked
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        log.info("[REFRESH-TOKEN] Rotated refresh token for user: {}", oldToken.getUser().getUsername());
        return createRefreshToken(oldToken.getUser().getUsername(), ipAddress, userAgent);
    }

    private Users findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
    }

    /**
     * Scheduled cleanup of expired refresh tokens
     * Runs every day at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            Instant now = Instant.now();
            refreshTokenRepository.deleteByExpiryDateBefore(now);
            log.info("[REFRESH-TOKEN] Cleaned up expired refresh tokens at {}", now);
        } catch (Exception e) {
            log.error("[REFRESH-TOKEN] Error during refresh token cleanup: {}", e.getMessage(), e);
        }
    }
}