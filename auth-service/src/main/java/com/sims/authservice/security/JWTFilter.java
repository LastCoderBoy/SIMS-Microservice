package com.sims.authservice.security;

import com.sims.authservice.exception.JwtAuthenticationException;
import com.sims.authservice.service.impl.JWTService;
import com.sims.authservice.service.impl.UserDetailsServiceImpl;
import com.sims.common.utils.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * Validates JWT tokens and sets authentication in SecurityContext
 *
 * @author LastCoderBoy
 * @since 2025-01-22
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String userInfo = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    token = TokenUtils.extractToken(authHeader);

                    // Check if token is blacklisted
                    if (jwtService.isTokenBlacklisted(token)) {
                        throw new JwtAuthenticationException("Token has been blacklisted");
                    }

                    userInfo = jwtService.extractUsername(token);

                } catch (ExpiredJwtException e) {
                    setErrorMessage(request, "Token has expired");
                    throw new JwtAuthenticationException("Token has expired");
                } catch (JwtException e) {
                    setErrorMessage(request, "Invalid token format");
                    throw new JwtAuthenticationException("Invalid token format");
                } catch (JwtAuthenticationException e) {
                    throw e;
                } catch (Exception e) {
                    setErrorMessage(request, "Authentication failed");
                    throw new JwtAuthenticationException("Authentication failed");
                }
            }

            if (userInfo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = context.getBean(UserDetailsServiceImpl.class)
                            .loadUserByUsername(userInfo);

                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("[JWT-FILTER] Authentication successful for user: {}", userInfo);
                    } else {
                        setErrorMessage(request, "Invalid or expired token");
                        throw new JwtAuthenticationException("Invalid token");
                    }
                } catch (UsernameNotFoundException e) {
                    setErrorMessage(request, "User not found");
                    throw new JwtAuthenticationException("User not found");
                } catch (JwtAuthenticationException e) {
                    throw e;
                } catch (Exception e) {
                    setErrorMessage(request, "Authentication failed");
                    throw new JwtAuthenticationException("Authentication failed");
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[JWT-FILTER] Unexpected error: {}", e.getMessage(), e);
            setErrorMessage(request, "Internal authentication error");
            throw new JwtAuthenticationException("Internal authentication error");
        }
    }

    /**
     * Set error message in request attribute for exception handler
     */
    private void setErrorMessage(HttpServletRequest request, String message) {
        request.setAttribute("jwt_error_message", message);
        log.warn("[JWT-FILTER] JWT Authentication error: {}", message);
    }
}