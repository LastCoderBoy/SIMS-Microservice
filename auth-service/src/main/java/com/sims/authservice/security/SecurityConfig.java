package com.sims.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sims.authservice.security.JWTFilter;
import com.sims.authservice.service.impl.UserDetailsServiceImpl;
import com.sims.common.models.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for Auth Service
 *
 * @author LastCoderBoy
 * @since 2025-01-20
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()

                        // Actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()

                        // All other endpoints for the AUTH-SERVICE require authentication
                        .anyRequest().authenticated())

                .exceptionHandling(exception -> exception
                        // Handle access denied (403)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("[AUTH-SERVICE] Access Denied: {}", accessDeniedException.getMessage());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            ApiResponse<Void> apiResponse = ApiResponse.error(
                                    "You do not have access to this resource"
                            );

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(apiResponse));
                        })

                        // Handle authentication failures (401)
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("[AUTH-SERVICE] Authentication Failed: {}", authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            String errorMessage = (String) request.getAttribute("jwt_error_message");
                            if (errorMessage == null) {
                                errorMessage = "Authentication failed: " + authException.getMessage();
                            }

                            ApiResponse<Void> apiResponse = ApiResponse.error(errorMessage);

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(apiResponse));
                        }))

                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsServiceImpl);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}