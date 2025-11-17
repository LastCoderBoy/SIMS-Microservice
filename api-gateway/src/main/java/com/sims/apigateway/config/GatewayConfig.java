package com.sims.apigateway.config;

import com.sims.apigateway.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Configuration
 * Applies custom JWT filter to protected routes
 *
 * This OVERRIDES routes defined in application.properties
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ==========================================
                // AUTH SERVICE - PUBLIC (No JWT required)
                // ==========================================
                .route("auth-public", r -> r
                        .path("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh")
                        .uri("lb://auth-service"))

                // ==========================================
                // AUTH SERVICE - PROTECTED (JWT required)
                // ==========================================
                .route("auth-protected", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://auth-service"))

                // ==========================================
                // SIMS CORE - ALL PROTECTED (JWT required)
                // ==========================================
                .route("products-service", r -> r
                        .path("/api/v1/products/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://sims-core-service"))

                .route("inventory-service", r -> r
                        .path("/api/v1/inventory/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://sims-core-service"))

                .route("orders-service", r -> r
                        .path("/api/v1/orders/**", "/api/v1/purchase-orders/**", "/api/v1/sales-orders/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://sims-core-service"))

                // ==========================================
                // SIMS CORE - PUBLIC EMAIL (no JWT required)
                // ==========================================
                .route("email-service", r -> r
                        .path("/api/v1/email/**")
                        .uri("lb://sims-core-service"))

                .build();
    }
}