package com.sims.common.utils;

import com.sims.common.exceptions.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;

import static com.sims.common.constants.AppConstants.BEARER_PREFIX;
import static com.sims.common.constants.AppConstants.BEARER_PREFIX_LENGTH;

/**
 * Utility class for JWT token operations
 * Shared across all SIMS microservices that handle authentication
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Slf4j
public final class TokenUtils {

    // Private constructor to prevent instantiation
    private TokenUtils() {
        throw new UnsupportedOperationException("TokenUtils is a utility class and cannot be instantiated");
    }

    /**
     * Extracts JWT token from Authorization header
     *
     * Expected format: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * Returns: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     *
     * @param authorizationHeader Authorization header value
     * @return JWT token without "Bearer " prefix
     * @throws InvalidTokenException if header is null, empty, or has invalid format
     */
    public static String extractToken(String authorizationHeader) {
        // Null/empty check
        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            log.error("TokenUtils: Authorization header is null or empty");
            throw new InvalidTokenException("Authorization header is missing");
        }

        // Bearer prefix check
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.error("TokenUtils: Authorization header doesn't start with 'Bearer '");
            throw new InvalidTokenException(
                    "Invalid token format. Expected format: 'Bearer <token>'"
            );
        }

        // Extract and trim token
        String token = authorizationHeader.substring(BEARER_PREFIX_LENGTH).trim();

        if (token.isEmpty()) {
            log.error("TokenUtils: Token contains only whitespace");
            throw new InvalidTokenException("Token is empty");
        }

        log.debug("TokenUtils: Successfully extracted token (length: {})", token.length());
        return token;
    }

    /**
     * Alias method for consistency with existing codebases
     *
     * @param authorizationHeader Authorization header value
     * @return JWT token without "Bearer " prefix
     */
    public static String validateAndExtractToken(String authorizationHeader) {
        return extractToken(authorizationHeader);
    }
}