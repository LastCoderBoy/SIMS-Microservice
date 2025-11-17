package com.sims.apigateway.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sims.common.models.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Exception handler for API Gateway
 * Handles gateway-level errors (routing failures, circuit breaker, etc.)
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Component
@Order(-1)  // High priority
@Slf4j
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        // Determine HTTP status based on exception type
        HttpStatus status = determineHttpStatus(ex);

        // Log error with appropriate level
        if (status.is5xxServerError()) {
            log.error("[API-GATEWAY] Server error: {}", ex.getMessage(), ex);
        } else {
            log.warn("[API-GATEWAY] Client error: {}", ex.getMessage());
        }

        // Set response status and content type
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Create error response
        ApiResponse<Void> errorResponse = ApiResponse.error(getUserFriendlyMessage(ex, status));

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

            return exchange.getResponse().writeWith(Mono.just(buffer));

        } catch (JsonProcessingException e) {
            log.error("[API-GATEWAY] Failed to serialize error response", e);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * Determine HTTP status from exception type
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return (HttpStatus) ((ResponseStatusException) ex).getStatusCode();
        }

        // Default to 500 for unexpected errors
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Get user-friendly error message
     */
    private String getUserFriendlyMessage(Throwable ex, HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return "The requested resource was not found";
        }

        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "Service is temporarily unavailable. Please try again later";
        }

        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return "Request timeout. Please try again";
        }

        if (status.is5xxServerError()) {
            return "An error occurred while processing your request. Please try again later";
        }

        // For client errors, return the actual message
        return ex.getMessage() != null ? ex.getMessage() : "An error occurred";
    }
}