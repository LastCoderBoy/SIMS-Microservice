package com.sims.simscoreservice.shared.util;

import com.sims.simscoreservice.exceptions.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class RoleValidator {

    /**
     * Require user to have at least one of the specified roles
     * @throws ForbiddenException if user doesn't have any required role
     */
    public void requireAnyRole(String userRoles, String... requiredRoles) {
        if (userRoles == null || userRoles.trim().isEmpty()) {
            log.warn("[ROLE-VALIDATOR] Missing user roles header");
            throw new ForbiddenException("Missing authentication information");
        }

        for (String role : requiredRoles) {
            if (userRoles.contains(role)) {
                log.debug("[ROLE-VALIDATOR] User has required role: {}", role);
                return;
            }
        }

        log.warn("[ROLE-VALIDATOR] Access denied - Required: {}, User has: {}",
                Arrays.toString(requiredRoles), userRoles);
        throw new ForbiddenException("Insufficient permissions.  Required: " + String.join(" or ", requiredRoles));
    }

    /**
     * Check if user has any of the specified roles (no exception)
     */
    public boolean hasAnyRole(String userRoles, String... requiredRoles) {
        if (userRoles == null || userRoles.trim().isEmpty()) {
            return false;
        }
        return Arrays.stream(requiredRoles)
                .anyMatch(userRoles::contains);
    }
}
