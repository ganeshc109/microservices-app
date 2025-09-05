package com.example.demo.util;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {

    private final PrivateKey privateKey;  // injected from JwtConfig
    private final PublicKey publicKey;    // injected from JwtConfig
    private final long expirationMs = 24 * 60 * 60 * 1000; // 1 day

    // Generate token using RSA private key
    public String generateToken(String username, String role) {
        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
        log.info("🟢 Generated JWT for {}: {}", username, token);
        return token;
    }

    // Parse token using RSA public key
    public Claims parseToken(String token) {
        log.info("🔍 Parsing JWT: {}", token);
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            log.info("✅ JWT is valid");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("❌ JWT validation error: {}", e.getMessage());
            return false;
        }
    }
}
