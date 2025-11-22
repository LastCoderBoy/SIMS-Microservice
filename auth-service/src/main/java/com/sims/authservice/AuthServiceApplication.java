package com.sims.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Auth Service - Authentication & Authorization Microservice
 *
 * Responsibilities:
 * - User login/logout
 * - JWT token generation
 * - Refresh token management
 * - Token blacklisting
 * - User profile updates
 *
 * Endpoints:
 * - POST /api/v1/auth/login
 * - POST /api/v1/auth/refresh
 * - POST /api/v1/auth/logout
 * - POST /api/v1/auth/logout-all
 * - PUT  /api/v1/auth/update
 *
 * @author LastCoderBoy
 * @since 2025-01-20
 */
@SpringBootApplication
@EnableDiscoveryClient  // Register with Eureka
@EnableScheduling       // Enable scheduled tasks (token cleanup)
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);

        System.out.println("""
            
            ╔════════════════════════════════════════════════════════════╗
            ║                AUTH SERVICE STARTED                        ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Service:    auth-service                                  ║
            ║  Port:       Dynamic (registered with Eureka)              ║
            ║  Eureka:     http://localhost:8761                         ║
            ║  API Docs:   /api/v1/auth/**                               ║
            ╚════════════════════════════════════════════════════════════╝
            
            """);
    }
}