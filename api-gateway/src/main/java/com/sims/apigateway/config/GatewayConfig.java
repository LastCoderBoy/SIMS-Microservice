package com.sims.apigateway.config;

import com.sims.apigateway.security.filter.JwtAuthenticationFilter;
import com.sims.common.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.sims.common.constants.AppConstants.*;

/**
 * Gateway Configuration
 * Applies custom JWT filter to protected routes
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
                        .path(AppConstants.PUBLIC_PATHS.toArray(new String[0]))
                        .uri("lb://" + AUTH_SERVICE))

                // ==========================================
                // AUTH SERVICE - PROTECTED (JWT required)
                // ==========================================
                .route("auth-protected", r -> r
                        .path(BASE_AUTH_PATH + "/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://" + AUTH_SERVICE))

                // ==========================================
                // SIMS CORE - ALL PROTECTED (JWT required)
                // ==========================================
                .route("products-service", r -> r
                        .path(BASE_PRODUCTS_PATH + "/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://" + SIMS_CORE_SERVICE))

                .route("inventory-service", r -> r
                        .path(BASE_INVENTORY_PATH + "/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://" + SIMS_CORE_SERVICE))

                .route("orders-service", r -> r
                        .path( BASE_ORDERS_PATH +"/**",
                                BASE_PURCHASE_ORDERS_PATH + "/**",
                                BASE_SALES_ORDERS_PATH + "/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://" + SIMS_CORE_SERVICE))

                // ==========================================
                // SIMS CORE - PUBLIC EMAIL (no JWT required)
                // ==========================================
                .route("email-service", r -> r
                        .path(BASE_EMAIL_PATH + "/**")
                        .uri("lb://" + SIMS_CORE_SERVICE))

                .build();
    }
}