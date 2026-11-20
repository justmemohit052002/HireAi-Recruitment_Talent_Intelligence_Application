package com.hireai.auth.security;

import com.hireai.auth.entity.Role;
import com.hireai.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${hireai.security.jwt.secret}") String secret,
            @Value("${hireai.security.jwt.access-token-expiration-ms:1800000}") long accessTokenExpirationMs,
            @Value("${hireai.security.jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMs
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        String orgId = user.getOrganization() != null ? user.getOrganization().getId().toString() : null;

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getWorkEmail())
                .claim("fullName", user.getFullName())
                .claim("orgId", orgId)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("tokenType", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractWorkEmail(String token) {
        Claims claims = extractClaims(token);
        return claims.get("email", String.class);
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
