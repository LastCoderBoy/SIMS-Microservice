package com.sims.simscoreservice.admin.controller;

import com.sims.simscoreservice.shared.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class BaseAdminController {

    @Autowired
    private RoleValidator roleValidator;

    /**
     * Validate ADMIN role from header
     */
    protected void requireAdmin(String roles) {
        roleValidator.requireAnyRole(roles, "ROLE_ADMIN");
    }

    /**
     * Validate any of the required roles
     */
    protected void requireAnyRole(String roles, String... requiredRoles) {
        roleValidator.requireAnyRole(roles, requiredRoles);
    }
}
