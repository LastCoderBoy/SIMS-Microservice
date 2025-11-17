package com.sims.apigateway.security;

import com.sims.common.exceptions.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * JWT Token Provider for API Gateway
 * Validates tokens (does NOT generate them - that's Auth Service's job)
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private SecretKey secretKey;
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenValidity;

    /**
     * Initialize SecretKey after dependency injection
     * Validates secret meets minimum requirements
     */
    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);

            if (keyBytes.length < 32) {
                throw new IllegalArgumentException(
                        "JWT secret must be at least 32 bytes (256 bits) for HS256. " +
                                "Current length: " + keyBytes.length + " bytes"
                );
            }

            this.secretKey = Keys.hmacShaKeyFor(keyBytes);

            log.info("[JWT-PROVIDER] Initialized successfully");
            log.info("[JWT-PROVIDER] Token validity: {} ms ({} minutes)",
                    accessTokenValidity, accessTokenValidity / 60000);

        } catch (IllegalArgumentException e) {
            log.error("[JWT-PROVIDER] Invalid JWT secret: {}", e.getMessage());
            throw new IllegalStateException("Failed to initialize JWT provider: Invalid secret", e);
        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Unexpected error during initialization: {}", e.getMessage());
            throw new IllegalStateException("Failed to initialize JWT provider", e);
        }
    }

    /**
     * Validate JWT token
     * Checks signature, expiration, and format
     *
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            log.debug("[JWT-PROVIDER] Token validated successfully for user: {}",
                    claims.getPayload().getSubject());
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("[JWT-PROVIDER] Token expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("[JWT-PROVIDER] Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("[JWT-PROVIDER] Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("[JWT-PROVIDER] JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Unexpected error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username from token
     *
     * @param token JWT token
     * @return username (subject claim)
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();

        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Failed to extract username from token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to extract username from token");
        }
    }

    /**
     * Extract user roles from token
     *
     * @param token JWT token
     * @return list of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("roles", List.class);

        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Failed to extract roles from token: {}", e.getMessage());
            return List.of(); // Return empty list if roles not found
        }
    }

    /**
     * Get token expiration date
     *
     * @param token JWT token
     * @return expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();

        } catch (Exception e) {
            log.error("[JWT-PROVIDER] Failed to extract expiration from token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to extract expiration from token");
        }
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }
}
