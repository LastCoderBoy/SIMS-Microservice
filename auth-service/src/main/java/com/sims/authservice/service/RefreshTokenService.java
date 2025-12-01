package com.sims.authservice.service;

import com.sims.authservice.entity.RefreshToken;
import com.sims.authservice.entity.Users;

/**
 * Refresh Token Service Interface
 *
 * @author LastCoderBoy
 * @since 2025-01-20
 */
public interface RefreshTokenService {

    RefreshToken createRefreshToken(String username, String ipAddress, String userAgent);
    RefreshToken verifyExpiration(String token);
    void revokeToken(String token);
    void revokeAllUserTokens(Users user);
    RefreshToken rotateRefreshToken(RefreshToken oldToken, String ipAddress, String userAgent);
    RefreshToken findByToken(String token);
}