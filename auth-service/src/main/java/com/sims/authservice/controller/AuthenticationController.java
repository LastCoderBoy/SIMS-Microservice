package com.sims.authservice.controller;

import com.sims.authservice.dto.LoginRequest;
import com.sims.authservice.dto.TokenResponse;
import com.sims.authservice.dto.UpdateUserRequest;
import com.sims.authservice.service.impl.UserService;
import com.sims.common.models.ApiResponse;
import com.sims.common.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles login, logout, token refresh, and user updates
 *
 * @author LastCoderBoy
 * @since 2025-01-22
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final UserService userService;

    /**
     * Login endpoint
     * Returns access token in response body, refresh token in HttpOnly cookie
     *
     * @param loginRequest Login credentials (username/email + password)
     * @param response HTTP response (for setting cookie)
     * @param request HTTP request (for IP tracking)
     * @return TokenResponse with access token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response,
            HttpServletRequest request) {

        log.info("[AUTH-CONTROLLER] Login attempt for user: {}", loginRequest.getLogin());

        TokenResponse tokenResponse = userService.verify(loginRequest, response, request);

        log.info("[AUTH-CONTROLLER] User '{}' logged in successfully", loginRequest.getLogin());

        return ResponseEntity.ok(
                ApiResponse.success("User logged in successfully", tokenResponse)
        );
    }

    /**
     * Refresh token endpoint
     * Reads refresh token from cookie and generates new access token
     * Called when access token expires (401 Unauthorized)
     *
     * @param request HTTP request (for reading cookie)
     * @param response HTTP response (for updating cookie)
     * @return TokenResponse with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("[AUTH-CONTROLLER] Refresh token requested");

        TokenResponse tokenResponse = userService.refreshToken(response, request);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", tokenResponse)
        );
    }

    /**
     * Logout endpoint
     * Revokes refresh token and clears cookie
     * Blacklists access token
     *
     * @param accessToken Authorization header with access token
     * @param response HTTP response (for clearing cookie)
     * @param request HTTP request (for reading cookie)
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String accessToken,
            HttpServletResponse response,
            HttpServletRequest request) {

        String jwtToken = TokenUtils.extractToken(accessToken);
        userService.logout(jwtToken, response, request);

        log.info("[AUTH-CONTROLLER] User logged out successfully");

        return ResponseEntity.ok()
                .header("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"")
                .body(ApiResponse.success("User logged out successfully"));
    }

    /**
     * Logout from all devices endpoint
     * Revokes all refresh tokens for current user
     *
     * @param request HTTP request (for reading cookie)
     * @param response HTTP response (for clearing cookie)
     * @return Success message
     */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("[AUTH-CONTROLLER] Logout all devices requested");

        userService.logoutAllDevices(response, request);

        return ResponseEntity.ok(
                ApiResponse.success("User logged out successfully from all devices")
        );
    }

    /**
     * Update user endpoint
     * User can update: firstName, lastName, password
     * If password is updated, access token is blacklisted (re-login required)
     *
     * @param userRequest User data to update
     * @param token Authorization header with access token
     * @return Success message
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @RequestBody UpdateUserRequest userRequest,
            @RequestHeader("Authorization") String token) {

        String jwtToken = TokenUtils.extractToken(token);
        userService.updateUser(userRequest, jwtToken);

        log.info("[AUTH-CONTROLLER] User updated successfully");

        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully. Please re-login if you updated your password.")
        );
    }
}