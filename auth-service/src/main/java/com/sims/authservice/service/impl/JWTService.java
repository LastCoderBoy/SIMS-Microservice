package com.sims.authservice.service.impl;

import com.sims.authservice.repository.BlackListTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.sims.common.constants.AppConstants.ACCESS_TOKEN_DURATION_MS;

/**
 * JWT Service - Token Generation and Validation
 *
 * @author LastCoderBoy
 * @since 2025-01-20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JWTService {

    @Value("${jwt.secret}")
    private String secretKey;

    private final BlackListTokenRepository blackListTokenRepository;


    public String generateAccessToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of(role));
        claims.put("type", "ACCESS");

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_DURATION_MS))
                .and()
                .signWith(getKey())
                .compact();
    }


    public boolean isTokenBlacklisted(String token) {
        if (token != null && !token.isEmpty()) {
            return blackListTokenRepository.existsByToken(token);
        }
        return false;
    }

    /**
     * Cleanup expired blacklisted tokens
     * Runs every 2 hours
     */
    @Scheduled(fixedRate = 7200000)
    @Transactional
    public void cleanBlacklistedTokens() {
        try {
            Date currentTime = new Date();
            Date expirationThreshold = new Date(currentTime.getTime() - (1000 * 60 * 60 * 8));

            blackListTokenRepository.findAll().stream()
                    .filter(token -> token.getBlacklistedAt().before(expirationThreshold))
                    .forEach(blackListTokenRepository::delete);

            log.info("[JWT-SERVICE] Cleaned up expired blacklisted tokens");
        } catch (Exception e) {
            log.error("[JWT-SERVICE] Error during blacklisted tokens cleanup: {}", e.getMessage());
        }
    }

    /**
     * Get signing key
     */
    private SecretKey getKey() {
        byte[] keys = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keys);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract expiration date from token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token against user details
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}