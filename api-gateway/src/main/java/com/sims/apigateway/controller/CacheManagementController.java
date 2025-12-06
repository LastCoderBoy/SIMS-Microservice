package com.sims.apigateway.controller;

import com.sims.apigateway.security.service.TokenValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cache Management Controller
 * Internal endpoint for Auth Service to evict tokens from cache
 *
 * ⚠️ Internal use only - not exposed publicly
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@RestController
@RequestMapping("/internal/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheManagementController {

    private final TokenValidationService tokenValidationService;

    /**
     * Evict token from cache (called by auth-service after logout)
     *
     * Endpoint: POST /internal/cache/evict-token
     */
    @PostMapping("/evict-token")
    public ResponseEntity<EvictionResponse> evictToken(@RequestBody TokenEvictionRequest request) {

        if (request.token() == null || request.token().isEmpty()) {
            log.warn("[CACHE-EVICTION] Received empty token");
            return ResponseEntity.badRequest()
                    .body(new EvictionResponse(false, "Token cannot be empty"));
        }

        tokenValidationService.evictToken(request.token());

        log.info("[CACHE-EVICTION] Token evicted: {}...",
                request.token().substring(0, Math.min(20, request.token().length())));

        return ResponseEntity.ok(new EvictionResponse(true, "Token evicted successfully"));
    }

    /**
     * Request DTO
     */
    public record TokenEvictionRequest(String token) {}

    /**
     * Response DTO
     */
    public record EvictionResponse(boolean success, String message) {}
}
