package com.sims.apigateway.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Token Validation Service
 * Only caches VALID tokens (not blacklisted ones)
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenValidationService {

    private final WebClient.Builder webClientBuilder;
    private Cache<String, Boolean> tokenCache;

    @PostConstruct
    public void init() {
        // Cache valid tokens for 30 seconds
        this.tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(10000)
                .build();
    }

    /**
     * Check if token is valid (not blacklisted)
     * Caches ONLY valid results
     * Always checks Auth Service for potentially blacklisted tokens
     */
    public Mono<Boolean> isTokenValid(String token) {
        // Check cache first
        Boolean cachedResult = tokenCache.getIfPresent(token);
        if (cachedResult != null && cachedResult) { // Only use cache if token was VALID
            log.debug("[TOKEN-VALIDATION] Cache hit: token is valid");
            return Mono.just(true);
        }

        // Not in cache or was invalid → Check Auth Service
        log.debug("[TOKEN-VALIDATION] Cache miss or invalid token, calling Auth Service");

        return webClientBuilder.build()
                .post()
                .uri("lb://auth-service/internal/token/validate")
                .bodyValue(new TokenValidationRequest(token))
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .map(TokenValidationResponse::valid)
                .timeout(Duration.ofSeconds(2))
                .retryWhen(Retry.fixedDelay(1, Duration.ofMillis(500)))
                .doOnNext(isValid -> {
                    if (isValid) {
                        tokenCache.put(token, true);
                        log.debug("[TOKEN-VALIDATION] Token is valid, cached result");
                    } else {
                        // Don't cache invalid/blacklisted tokens
                        tokenCache.invalidate(token); // Remove from cache if present
                        log.debug("[TOKEN-VALIDATION] Token is invalid/blacklisted, NOT caching");
                    }
                })
                .doOnError(error -> log.error("[TOKEN-VALIDATION] Error: {}", error.getMessage()))
                .onErrorReturn(false); // Fail-safe: deny if Auth Service unreachable
    }

    /**
     * Evict token from cache (called when user logs out)
     */
    public void evictToken(String token) {
        tokenCache.invalidate(token);
        log.info("[TOKEN-VALIDATION] Token evicted from cache: {}...",
                token.substring(0, Math.min(20, token.length())));
    }

    /**
     * Request DTO
     */
    private record TokenValidationRequest(String token) {}

    /**
     * Response DTO
     */
    private record TokenValidationResponse(boolean valid, String message) {}
}