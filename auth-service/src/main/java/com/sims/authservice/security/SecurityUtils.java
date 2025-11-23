package com.sims.authservice.security;

import com.sims.authservice.service.impl.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security Utilities
 * Helper methods for security-related operations
 *
 * @author LastCoderBoy
 * @since 2025-01-22
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JWTService jwtService;

    /**
     * Check if current user has ADMIN or MANAGER role
     */
    public boolean hasAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(r ->
                        r.getAuthority().equals("ROLE_ADMIN") ||
                                r.getAuthority().equals("ROLE_MANAGER"));
    }

    /**
     * Validate JWT token and extract username
     */
    public String validateAndExtractUsername(String jwtToken) throws BadRequestException {
        String username = jwtService.extractUsername(jwtToken);
        if (username == null || username.isEmpty()) {
            throw new BadRequestException("Invalid JWT token: Cannot determine user.");
        }
        return username;
    }

    /**
     * Extract client IP address from request
     * Handles X-Forwarded-For header (proxy/load balancer)
     */
    public String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            // The first IP (client's real IP)
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}