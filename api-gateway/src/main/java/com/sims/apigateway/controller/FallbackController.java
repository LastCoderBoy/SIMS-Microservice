package com.sims.apigateway.controller;


import com.sims.common.models.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback Controller for Circuit Breaker
 * Provides user-friendly error messages when services are down
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /**
     * Fallback for Auth Service
     */
    @GetMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> authServiceFallback() {
        log.error("[FALLBACK] Auth service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Authentication service is temporarily unavailable. Please try again later."
                ));
    }

    /**
     * Fallback for SIMS Core Service
     */
    @GetMapping("/core")
    public ResponseEntity<ApiResponse<Void>> coreServiceFallback() {
        log.error("[FALLBACK] SIMS Core service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Service is temporarily unavailable. Please try again later."
                ));
    }

    /**
     * Generic fallback
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<Void>> defaultFallback() {
        log.error("[FALLBACK] Service is currently unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Service is temporarily unavailable. Our team has been notified. Please try again later."
                ));
    }
}
