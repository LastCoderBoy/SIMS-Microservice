package com.sims.authservice.controller;

import com.sims.authservice.dto.TokenValidationRequest;
import com.sims.authservice.dto.TokenValidationResponse;
import com.sims.authservice.service.impl.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.sims.common.constants.AppConstants.BASE_AUTH_PATH;

/**
 * Token Validation Controller
 * Internal endpoint for API Gateway to validate tokens
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping("/internal/token")
@RequiredArgsConstructor
@Slf4j
public class TokenValidationController {

    private final JWTService jwtService;

    /**
     * Validate token (check blacklist)
     * Internal endpoint - only accessible from API Gateway
     */
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        try {
            String token = request.token();

            // Check if token is blacklisted
            boolean isBlacklisted = jwtService.isTokenBlacklisted(token);

            if (isBlacklisted) {
                log.debug("[TOKEN-VALIDATION] Token is blacklisted");
                return ResponseEntity.ok(new TokenValidationResponse(false, "Token is blacklisted"));
            }

            return ResponseEntity.ok(new TokenValidationResponse(true, "Token is valid"));

        } catch (Exception e) {
            log.error("[TOKEN-VALIDATION] Error validating token: {}", e.getMessage());
            return ResponseEntity.ok(new TokenValidationResponse(false, "Invalid token"));
        }
    }
}
