package com.sims.authservice.service.impl;

import com.sims.authservice.dto.LoginRequest;
import com.sims.authservice.dto.TokenResponse;
import com.sims.authservice.dto.UpdateUserRequest;
import com.sims.authservice.entity.BlacklistedToken;
import com.sims.authservice.entity.RefreshToken;
import com.sims.authservice.entity.Users;
import com.sims.authservice.exception.AuthenticationFailedException;
import com.sims.authservice.exception.JwtAuthenticationException;
import com.sims.authservice.exception.PasswordValidationException;
import com.sims.authservice.exception.TokenRefreshException;
import com.sims.authservice.repository.BlackListTokenRepository;
import com.sims.authservice.repository.UserRepository;
import com.sims.authservice.security.SecurityUtils;
import com.sims.authservice.security.UserPrincipal;
import com.sims.authservice.service.RefreshTokenService;
import com.sims.common.exceptions.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

/**
 * User Service - Authentication Business Logic
 *
 * @author LastCoderBoy
 * @since 2025-01-20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Value("${jwt.refresh.cookie.name}")
    private String refreshTokenCookieName;

    @Value("${jwt.refresh.cookie.max-age}")
    private int refreshTokenCookieMaxAge;

    private final WebClient.Builder webClientBuilder;

    // Dependencies
    private final AuthenticationManager authManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    // Services
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    // Repositories
    private final BlackListTokenRepository blackListTokenRepository;
    private final UserRepository userRepository;

    /**
     * Login - Authenticate user and generate tokens
     */
    public TokenResponse verify(LoginRequest loginRequest, HttpServletResponse response, HttpServletRequest request) {
        try {
            Authentication authentication = authManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getLogin(),
                            loginRequest.getPassword()
                    ));

            if (authentication.isAuthenticated()) {
                // Generate Access Token
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                String username = userPrincipal.getUsername();
                String role = userPrincipal.getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("ROLE_STAFF");

                String accessToken = jwtService.generateAccessToken(username, role);

                // Generate Refresh Token
                String userAgent = request.getHeader("User-Agent");
                String ipAddress = securityUtils.extractClientIp(request);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                        username,
                        ipAddress,
                        userAgent
                );

                // Set refresh token in HttpOnly cookie
                setRefreshTokenCookie(response, refreshToken.getToken());

                log.info("[USER-SERVICE] User '{}' logged in successfully from IP: {}", username, ipAddress);
                return new TokenResponse(accessToken, "Bearer", 3600L, username, role);
            }

            throw new BadCredentialsException("Invalid credentials");

        } catch (BadCredentialsException e) {
            log.warn("[USER-SERVICE] Invalid credentials for user: {}", loginRequest.getLogin());
            throw new AuthenticationFailedException("Invalid credentials");
        } catch (Exception e) {
            log.error("[USER-SERVICE] Unexpected authentication error for user: {}. Reason: {}",
                    loginRequest.getLogin(), e.getMessage(), e);
            throw new AuthenticationFailedException("Unexpected authentication error");
        }
    }

    /**
     * Refresh Token - Generate new access token from refresh token
     */
    public TokenResponse refreshToken(HttpServletResponse response, HttpServletRequest request) {
        try {
            // Extract refresh token from cookie
            String requestRefreshToken = extractRefreshTokenFromCookie(request);

            if (requestRefreshToken == null) {
                throw new TokenRefreshException("Request does not contain a refresh token");
            }

            RefreshToken refreshToken = refreshTokenService.verifyExpiration(requestRefreshToken);

            // Verify IP hasn't changed (detect token theft)
            String currentIp = securityUtils.extractClientIp(request);
            String storedIp = refreshToken.getIpAddress();

            if (!currentIp.equals(storedIp)) {
                log.warn("[USER-SERVICE] Refresh token used from different IP. Original: {}, Current: {}",
                        storedIp, currentIp);
                throw new TokenRefreshException("IP mismatch detected");
            }

            // Generate new access token
            String username = refreshToken.getUser().getUsername();
            String role = refreshToken.getUser().getRole().name();
            String accessToken = jwtService.generateAccessToken(username, role);

            // Rotate refresh token
            String userAgent = request.getHeader("User-Agent");
            RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                    refreshToken,
                    currentIp,
                    userAgent
            );

            // Update cookie
            setRefreshTokenCookie(response, newRefreshToken.getToken());

            log.info("[USER-SERVICE] Token refreshed for user: {}", username);
            return new TokenResponse(accessToken, "Bearer", 3600L, username, role);

        } catch (TokenRefreshException e) {
            clearRefreshTokenCookie(response);
            log.warn("[USER-SERVICE] Token refresh failed - {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            clearRefreshTokenCookie(response);
            log.error("[USER-SERVICE] Unexpected error during token refresh - {}", e.getMessage(), e);
            throw new TokenRefreshException("Failed to refresh token");
        }
    }

    /**
     * Logout - Revoke tokens and clear cookie
     */
    @Transactional
    public void logout(String jwtToken, HttpServletResponse response, HttpServletRequest request) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new InvalidTokenException("Token cannot be null or empty");
        }

        try {
            String username = jwtService.extractUsername(jwtToken);

            if (jwtService.isTokenBlacklisted(jwtToken)) {
                log.warn("[USER-SERVICE] Token already blacklisted");
                return;
            }

            // Blacklist access token
            blackListTokenRepository.save(new BlacklistedToken(jwtToken, new Date()));

            // Notify API Gateway to evict from cache (async, fire-and-forget)
            notifyGatewayToEvictToken(jwtToken);

            // Revoke refresh token
            String refreshToken = extractRefreshTokenFromCookie(request);
            if (refreshToken != null) {
                refreshTokenService.revokeToken(refreshToken);
            }

            // Clear cookie
            clearRefreshTokenCookie(response);

            log.info("[USER-SERVICE] User '{}' logged out successfully", username);

        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token format");
        } catch (JwtAuthenticationException e) {
            throw new JwtAuthenticationException("Invalid token");
        }
    }

    /**
     * Logout from all devices - Revoke all refresh tokens
     */
    public void logoutAllDevices(HttpServletResponse response, HttpServletRequest request) {
        try {
            String requestRefreshToken = extractRefreshTokenFromCookie(request);

            if (requestRefreshToken == null) {
                throw new TokenRefreshException("Request does not contain a refresh token");
            }

            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);
            Users user = refreshToken.getUser();

            refreshTokenService.revokeAllUserTokens(user);
            clearRefreshTokenCookie(response);

            log.info("[USER-SERVICE] User '{}' logged out from all devices", user.getUsername());

        } catch (TokenRefreshException e) {
            clearRefreshTokenCookie(response);
            log.warn("[USER-SERVICE] Logout all devices failed - {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            clearRefreshTokenCookie(response);
            log.error("[USER-SERVICE] Unexpected error during logout all - {}", e.getMessage(), e);
            throw new TokenRefreshException("Internal service error");
        }
    }

    /**
     * Update User - Update newUserInfo profile
     * If password is updated, access token is blacklisted (re-login required)
     */
    @Transactional
    public void updateUser(UpdateUserRequest userRequest, String currentAccessToken) {
        if (currentAccessToken == null || currentAccessToken.isEmpty()) {
            throw new InvalidTokenException("Invalid token provided");
        }

        try {
            String username = jwtService.extractUsername(currentAccessToken);
            if (username == null) {
                throw new InvalidTokenException("Could not extract username from token");
            }

            Users currentUser = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            updateUserFields(currentUser, userRequest, currentAccessToken);
            userRepository.save(currentUser);

            log.info("[USER-SERVICE] User '{}' updated successfully", username);

        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token is expired");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token format");
        }
    }

    private void updateUserFields(Users currentUser, UpdateUserRequest userRequest, String currentAccessToken) {
        if (userRequest.getPassword() != null) {
            String newPassword = userRequest.getPassword();

            if (!isValidPassword(newPassword)) {
                log.warn("[USER-SERVICE] Invalid password format");
                throw new PasswordValidationException(
                        "Password must contain at least 8 characters, including 1 uppercase, " +
                                "1 lowercase, 1 number and 1 special character (@#$%^&*()-_+)."
                );
            }

            currentUser.setPassword(passwordEncoder.encode(newPassword));
            blackListTokenRepository.save(new BlacklistedToken(currentAccessToken, new Date()));

            log.info("[USER-SERVICE] User '{}' password updated. Token invalidated.", currentUser.getUsername());
        }

        if (userRequest.getFirstName() != null) {
            currentUser.setFirstName(userRequest.getFirstName());
        }

        if (userRequest.getLastName() != null) {
            currentUser.setLastName(userRequest.getLastName());
        }
    }

    /**
     * Set refresh token in HttpOnly cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenCookieMaxAge);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);

        log.debug("[USER-SERVICE] Set refresh token cookie");
    }

    /**
     * Clear refresh token cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    /**
     * Extract refresh token from cookie
     */
    private @Nullable String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Validate password strength
     */
    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_])(?=\\S+$).{8,}$";
        return password.matches(passwordRegex);
    }

    /**
     * Notify API Gateway to evict token from cache
     * Async (fire-and-forget) - logout succeeds even if Gateway is down
     */
    private void notifyGatewayToEvictToken(String token) {
        webClientBuilder.build()
                .post()
                .uri("lb://api-gateway/internal/cache/evict-token") // Load-balanced via Eureka
                .bodyValue(new TokenEvictionRequest(token))
                .retrieve()
                . bodyToMono(EvictionResponse.class)
                .timeout(Duration.ofSeconds(2)) // 2-second timeout
                .doOnSuccess(response -> {
                    if (response != null && response.success()) {
                        log.debug("[CACHE-EVICTION] Gateway cache evicted successfully");
                    } else {
                        log.warn("[CACHE-EVICTION] Gateway returned failure: {}",
                                response != null ? response.message() : "null");
                    }
                })
                .doOnError(error -> log.warn("[CACHE-EVICTION] Failed to notify gateway: {} - {}",
                        error.getClass().getSimpleName(), error.getMessage()))
                .onErrorResume(e -> Mono.empty()) // Don't fail logout if gateway is down
                .subscribe(); // Fire and forget (async)
    }

    /**
     * Request DTO for cache eviction
     */
    private record TokenEvictionRequest(String token) {}

    /**
     * Response DTO from Gateway
     */
    private record EvictionResponse(boolean success, String message) {}

}
