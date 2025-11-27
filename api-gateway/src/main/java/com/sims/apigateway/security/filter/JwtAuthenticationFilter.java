package com.sims.apigateway.security.filter;

import com.sims.apigateway.security.JwtTokenProvider;
import com.sims.common.utils.TokenUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sims.common.constants.AppConstants.USER_ID_HEADER;
import static com.sims.common.constants.AppConstants.USER_ROLES_HEADER;

/**
 * JWT Authentication Filter for API Gateway
 * Validates JWT tokens before forwarding requests to backend services
 *
 * Applied to protected routes only (not login/register)
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    // List of public endpoints (no auth required)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/email",
            "/actuator/health",
            "/actuator/info"
    );

    private final PathMatcher pathMatcher;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.pathMatcher = new AntPathMatcher();
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            log.debug("[JWT-FILTER] Processing request: {} {}", request.getMethod(), path);

            // Skip authentication for public paths
            if (isPublicPath(path, config)) {
                log.debug("[JWT-FILTER] Public path, skipping authentication: {}", path);
                return chain.filter(exchange);
            }

            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("[JWT-FILTER] Missing Authorization header for path: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            try {
                String token = TokenUtils.extractToken(authHeader);

                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("[JWT-FILTER] Invalid token for path: {}", path);
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user information from token
                String username = jwtTokenProvider.getUsernameFromToken(token);
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);

                log.debug("[JWT-FILTER] Token validated for user: {} with roles: {}", username, roles);

                // Add user info to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(USER_ID_HEADER, username)
                        .header(USER_ROLES_HEADER, String.join(",", roles))
                        .build();

                // Forward to backend service
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("[JWT-FILTER] Token validation failed: {}", e.getMessage());
                return onError(exchange, "Token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Check if path is public (no authentication required)
     */
    private boolean isPublicPath(String path, Config config) {
        // Check global public paths
        boolean isGlobalPublic = PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        // Check route-specific public paths
        boolean isRoutePublic = config.getPublicPaths() != null &&
                config.getPublicPaths().stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, path));

        return isGlobalPublic || isRoutePublic;
    }

    /**
     * Return error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String errorJson = String.format(
                "{\"success\": false, \"message\": \"%s\", \"data\": null}",
                message
        );

        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));
    }

    /**
     * Configuration class for per-route filter customization
     */
    @Getter
    @Setter
    public static class Config {

        /**
         * Enable/disable JWT authentication for this route
         */
        private boolean enabled = true;

        /**
         * Additional public paths for this specific route (supports wildcards)
         * Example: ["/api/v1/products/catalog/**", "/api/v1/products/search"]
         */
        private List<String> publicPaths = new ArrayList<>();

        /**
         * Required roles for accessing this route
         * If empty, any authenticated user can access
         */
        private List<String> requiredRoles = new ArrayList<>();

        /**
         * Custom headers to add to downstream requests
         */
        private Map<String, String> customHeaders = new HashMap<>();

        /**
         * Custom unauthorized message
         */
        private String unauthorizedMessage = "Unauthorized access";

        /**
         * Custom forbidden message
         */
        private String forbiddenMessage = "Insufficient permissions";
    }
}
