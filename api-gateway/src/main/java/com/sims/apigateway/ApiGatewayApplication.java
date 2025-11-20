package com.sims.apigateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway - Single Entry Point for SIMS Microservices
 *
 * Responsibilities:
 * - Route incoming requests to appropriate microservices
 * - Service discovery via Eureka
 * - JWT token validation
 * - CORS handling
 * - Circuit breaker (fault tolerance)
 * - Rate limiting
 * - Request/response logging
 *
 * Routes:
 * - /api/v1/auth/**      → auth-service
 * - /api/v1/products/**  → sims-core-service
 * - /api/v1/inventory/** → sims-core-service
 * - /api/v1/orders/**    → sims-core-service
 *
 * Access: http://localhost:8080
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@SpringBootApplication
@EnableDiscoveryClient  // Register with Eureka and discover other services
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);

        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════════════════╗
            ║                 🚀 API GATEWAY STARTED                            ║
            ╠═══════════════════════════════════════════════════════════════════╣
            ║  Gateway URL:       http://localhost:8080                         ║
            ║  Eureka Dashboard:  http://localhost:8761                         ║
            ║  Actuator Health:   http://localhost:8080/actuator/health         ║
            ║  Gateway Routes:    http://localhost:8080/actuator/gateway/routes ║
            ╚═══════════════════════════════════════════════════════════════════╝
            
            """);
    }
}
