package com.sims.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server - Service Discovery for SIMS Microservices
 *
 * Provides:
 * - Service registration (microservices register themselves)
 * - Service discovery (microservices find each other)
 * - Load balancing (via Ribbon)
 * - Health monitoring
 * - Dashboard UI
 *
 * Dashboard Access: http://localhost:8761
 *
 * Registered Services:
 * - api-gateway (port 8080)
 * - auth-service (dynamic port)
 * - sims-core-service (dynamic port)
 *
 * @author LastCoderBoy
 * @since 2025-01-20
 */
@SpringBootApplication
@EnableEurekaServer  // Enable Eureka Server
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);

        System.out.println("""
            
            ╔════════════════════════════════════════════════════════════╗
            ║                 EUREKA SERVER STARTED                      ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Dashboard:        http://localhost:8761                   ║
            ║  Service Registry: http://localhost:8761/eureka/apps       ║
            ║  Actuator Health:  http://localhost:8761/actuator/health   ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Waiting for services to register...                       ║
            ║  - api-gateway                                             ║
            ║  - auth-service                                            ║
            ║  - sims-core-service                                       ║
            ╚════════════════════════════════════════════════════════════╝
            
            """);
    }
}