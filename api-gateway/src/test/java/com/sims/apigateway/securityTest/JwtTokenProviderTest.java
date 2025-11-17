package com.sims.apigateway.securityTest;

import com.sims.apigateway.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = JwtTokenProvider.class)
@ActiveProfiles("test")
class JwtTokenProviderTest {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    private String validToken;
    private String expiredToken;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        // Decode the secret exactly as JwtTokenProvider does
        String secret = "sDIWTw49KIYrSsxHzYQpkMtQYQGSTbpwXcwOJIdAcIo=";
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

        // Generate a valid token
        validToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();

        // Generate an expired token
        expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(secretKey)
                .compact();
    }


    @Test
    void testValidToken() {
        boolean isValid = jwtTokenProvider.validateToken(validToken);
        assertTrue(isValid);
    }

    @Test
    void testExpiredToken() {
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);
        assertFalse(isValid);
    }

    @Test
    void testExtractUsername() {
        String username = jwtTokenProvider.getUsernameFromToken(validToken);
        assertEquals("testuser", username);
    }
}
