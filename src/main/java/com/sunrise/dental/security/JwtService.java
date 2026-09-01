package com.sunrise.dental.security;

import com.sunrise.dental.constant.SecurityConstants;
import com.sunrise.dental.entity.User;
import com.sunrise.dental.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT creation, parsing and validation (Token pattern).
 */
@Service
public class JwtService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtService(@Value("${app.jwt.access-secret}") String accessSecret,
                      @Value("${app.jwt.refresh-secret}") String refreshSecret) {
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessKey, SecurityConstants.ACCESS_TOKEN_VALIDITY_MS);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshKey, SecurityConstants.REFRESH_TOKEN_VALIDITY_MS);
    }

    private String buildToken(User user, SecretKey key, long validityMs) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "email", user.getEmail(),
                "fullName", user.getFullName()
        );
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + validityMs))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject, accessKey);
    }

    public Role extractRole(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class), accessKey);
        return role == null ? null : Role.valueOf(role);
    }

    public boolean isAccessTokenValid(String token, String username) {
        return isTokenValid(token, username, accessKey);
    }

    public boolean isRefreshTokenValid(String token, String username) {
        return isTokenValid(token, username, refreshKey);
    }

    public String extractUsernameFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, refreshKey);
    }

    private boolean isTokenValid(String token, String username, SecretKey key) {
        final String extracted = extractClaim(token, Claims::getSubject, key);
        return extracted != null && extracted.equals(username) && !isTokenExpired(token, key);
    }

    private boolean isTokenExpired(String token, SecretKey key) {
        return extractClaim(token, Claims::getExpiration, key).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, SecretKey key) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
