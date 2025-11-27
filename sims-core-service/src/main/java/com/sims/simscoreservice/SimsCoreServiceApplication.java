package com.sims.simscoreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SIMS Core Service - Main Business Logic
 *
 * Responsibilities:
 * - Product Management (CRUD, search, categories)
 * - Inventory Management (stock tracking, adjustments)
 * - Purchase Orders (supplier orders)
 * - Sales Orders (customer orders)
 * - Report & Analytics
 *
 * Endpoints:
 * - /api/v1/products/**
 * - /api/v1/inventory/**
 * - /api/v1/purchase-orders/**
 * - /api/v1/sales-orders/**
 * - /api/v1/orders/**
 * - /api/v1/email/**
 * - /api/v1/analytics/**
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@SpringBootApplication
@EnableDiscoveryClient  // Register with Eureka
@EnableFeignClients     // Discover other services via Feign
@EnableScheduling
public class SimsCoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimsCoreServiceApplication.class, args);

        System.out.println("""
            
            ╔════════════════════════════════════════════════════════════╗
            ║             SIMS CORE SERVICE STARTED                      ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Service:    sims-core-service                             ║
            ║  Port:       Dynamic (registered with Eureka)              ║
            ║  Eureka:     http://localhost:8761                         ║
            ║  API Docs:   /api/v1/**                                    ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Modules:                                                  ║
            ║     Products Management                                    ║
            ║     Inventory Management                                   ║
            ║     Purchase Orders                                        ║
            ║     Sales Orders                                           ║
            ║     Email Notifications                                    ║
            ╚════════════════════════════════════════════════════════════╝
            
            """);
    }
}