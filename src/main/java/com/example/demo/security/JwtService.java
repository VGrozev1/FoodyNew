package com.example.demo.security;

import com.example.demo.Entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${foody.security.jwt.secret}") String secret,
            @Value("${foody.security.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        String normalizedSecret = normalizeSecret(secret);
        this.key = Keys.hmacShaKeyFor(normalizedSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(Map.of("role", user.getRole().name(), "userId", user.getId()))
                .subject(user.getEmail())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? String.valueOf(role) : null;
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String normalizeSecret(String secret) {
        if (secret == null) {
            return "foody-default-jwt-secret-please-change-me-123456";
        }
        String trimmed = secret.trim();
        if (trimmed.length() >= 32) {
            return trimmed;
        }
        StringBuilder sb = new StringBuilder(trimmed);
        while (sb.length() < 32) {
            sb.append("foody-secret-pad");
        }
        return sb.substring(0, Math.max(32, sb.length()));
    }
}
