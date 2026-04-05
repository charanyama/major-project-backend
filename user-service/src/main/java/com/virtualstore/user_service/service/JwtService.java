package com.virtualstore.user_service.service;

import com.virtualstore.user_service.config.JwtProperties;
import com.virtualstore.user_service.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * JwtService
 *
 * The ONLY place in the entire system that signs JWTs.
 * The API Gateway only verifies — it never signs.
 *
 * Token types:
 * Access token — short-lived (15 min default), sent with every API request
 * Refresh token — long-lived (7 days), used only to get a new access token
 *
 * Claims included in access tokens:
 * sub — userId (the user's MongoDB _id)
 * email — user's email
 * roles — list of role strings e.g. ["ROLE_USER"]
 * jti — unique token ID (UUID), used for blocklist lookup on signout
 *
 * Algorithm: HS256 (symmetric secret shared with the API Gateway)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Issues a signed access token for the given user.
     * The token's subject (sub) is the userId so downstream services
     * receive it via the X-User-Id header injected by the gateway.
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getAccessTokenExpiryMs());

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti — unique per token
                .subject(user.getId()) // sub — userId
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name()) // single role, not a list
                .claim("fullName", user.getFullName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Issues a refresh token.
     * Refresh tokens carry only the userId — no roles or email.
     * They are exchanged at POST /auth/refresh for a new access token.
     */
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getRefreshTokenExpiryMs());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Parses and validates a token.
     * Returns the Claims if valid, empty if expired or tampered.
     */
    public Optional<Claims> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT invalid: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Extracts the JTI (JWT ID) from a token WITHOUT verifying its signature.
     * Used during signout — the token may be valid or near-expired but we
     * still need the JTI to add it to the blocklist.
     *
     * This is safe because we only use it to blocklist — not to authenticate.
     */
    public Optional<String> extractJtiUnsafe(String token) {
        try {
            // Parse without verification to get claims even from expired tokens
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getId());
        } catch (ExpiredJwtException e) {
            // Even expired tokens have a JTI we can blocklist
            return Optional.ofNullable(e.getClaims().getId());
        } catch (JwtException e) {
            log.warn("Cannot extract JTI — token is malformed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extracts the expiry instant from a token for use as the blocklist TTL.
     * Same safety rationale as extractJtiUnsafe.
     */
    public Optional<Instant> extractExpiryUnsafe(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getExpiration())
                    .map(Date::toInstant);
        } catch (ExpiredJwtException e) {
            return Optional.ofNullable(e.getClaims().getExpiration())
                    .map(Date::toInstant);
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public Optional<String> extractUserId(String token) {
        return parseToken(token).map(Claims::getSubject);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}