package com.sims.common.constants;

import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;

/**
 * Application-wide constants shared across all SIMS microservices
 * Centralized configuration values to maintain consistency
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
public final class AppConstants {

    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("AppConstants is a utility class and cannot be instantiated");
    }

    // ========== API Versioning & Paths ==========
    public static final String BASE_URL = "http://localhost:8080";
    public static final String API_VERSION_V1 = "/api/v1";
    public static final List<String> PUBLIC_AUTH_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/internal/**",
            "/actuator/health",
            "/actuator/info"
    );
    public static final String BASE_ADMIN_PATH = API_VERSION_V1 + "/admin";
    public static final String BASE_AUTH_PATH = API_VERSION_V1 + "/auth";
    public static final String BASE_PRODUCTS_PATH = API_VERSION_V1 + "/products";
    public static final String BASE_INVENTORY_PATH = API_VERSION_V1 + "/inventory";
    public static final String BASE_ORDER_MANAGEMENT_PATH = API_VERSION_V1 + "/order-management";
    public static final String BASE_SUPPLIERS_PATH = API_VERSION_V1 + "/suppliers";
    public static final String BASE_EMAIL_PATH = API_VERSION_V1 + "/email";
    public static final String BASE_ANALYTICS_PATH = API_VERSION_V1 + "/analytics";

    // ========== Pagination ==========
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY_FOR_PO = "orderDate";
    public static final String DEFAULT_SORT_BY_FOR_SO = "orderReference";
    public static final String DEFAULT_SORT_DIRECTION = "desc";
    public static final String DEFAULT_SORT_BY = "product.name";

    // ========== JWT ==========
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
    public static final long ACCESS_TOKEN_DURATION_MS = 15 * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_DURATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

    // ========== Date/Time Formats ==========
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";

    // ========== HTTP Headers ==========
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String SERVICE_NAME_HEADER = "X-Service-Name";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String IP_ADDRESS_HEADER = "X-Forwarded-For";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    // ========== Service Names ==========
    public static final String AUTH_SERVICE = "auth-service";
    public static final String SIMS_CORE_SERVICE = "sims-core-service";
    public static final String NOTIFICATION_SERVICE = "notification-service";
    public static final String API_GATEWAY = "api-gateway";

    // ========== QR CODE CONFIG ==========
    public static final int QR_CODE_WIDTH = 300;
    public static final int QR_CODE_HEIGHT = 300;
    public static final String QR_CODE_S3_PREFIX = "qr-codes/";
    public static final Duration PRESIGNED_URL_DURATION = Duration.ofHours(1);
}