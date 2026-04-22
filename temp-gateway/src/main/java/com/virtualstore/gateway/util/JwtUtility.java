package com.virtualstore.gateway.util;

import com.virtualstore.gateway.config.JwtProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * JwtUtility
 *
 * Low-level JWT utility using the Nimbus JOSE+JWT library.
 *
 * Responsibilities:
 * 1. Build the SecretKey from the configured secret string (HS256).
 * 2. Parse and validate JWT tokens (signature + expiry).
 * 3. Extract individual claims, especially the userId claim.
 *
 * This class is used by:
 * - SecurityConfig → provides getSecretKey() to NimbusReactiveJwtDecoder
 * - JwtAuthFilter → extracts userId to inject into X-User-Id header
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtility {

    private final JwtProperties jwtProperties;

    // ─────────────────────────────────────────────────────────────────────────
    // Secret Key
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a javax.crypto.SecretKey (HMAC-SHA256) from the configured secret
     * string.
     * This key is used by both:
     * - NimbusReactiveJwtDecoder (Spring Security layer)
     * - DefaultJWTProcessor (Nimbus layer for manual claim parsing)
     */
    public SecretKey getSecretKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Token Parsing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Parses a raw JWT string and returns its claims.
     *
     * Validates:
     * - Signature (using HS256 + secret key)
     * - Expiration time (exp claim)
     *
     * @param token Raw JWT string (without "Bearer " prefix)
     * @return Optional containing JWTClaimsSet if valid, empty if invalid/expired
     */
    public Optional<JWTClaimsSet> parseToken(String token) {
        try {
            ConfigurableJWTProcessor<SecurityContext> processor = buildJwtProcessor();
            JWTClaimsSet claims = processor.process(token, null);
            return Optional.of(claims);
        } catch (Exception e) {
            log.warn("JWT parsing failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extracts the userId from a JWT token string.
     *
     * The claim name is configurable via jwt.user-id-claim (default: "sub").
     * Most auth servers put the user ID in the "sub" (subject) claim.
     *
     * @param token Raw JWT string
     * @return Optional containing the userId string, empty if not found
     */
    public Optional<String> extractUserId(String token) {
        return parseToken(token).map(claims -> {
            try {
                String claimName = jwtProperties.getUserIdClaim();
                Object value = claims.getClaim(claimName);
                if (value == null) {
                    log.warn("Claim '{}' not found in JWT", claimName);
                    return null;
                }
                return value.toString();
            } catch (Exception e) {
                log.warn("Failed to extract userId from JWT: {}", e.getMessage());
                return null;
            }
        });
    }

    /**
     * Extracts the raw Bearer token from an Authorization header value.
     *
     * @param authHeader Value of the Authorization header, e.g. "Bearer eyJ..."
     * @return The raw token string, or empty if header format is wrong
     */
    public Optional<String> extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7).trim());
        }
        return Optional.empty();
    }

    /**
     * Builds a Nimbus DefaultJWTProcessor configured for HS256.
     * For RS256, replace ImmutableSecret with a JWK set source.
     */
    private ConfigurableJWTProcessor<SecurityContext> buildJwtProcessor() {
        ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

        // Set the key selector — HS256 with our secret key
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                JWSAlgorithm.HS256,
                new ImmutableSecret<>(getSecretKey()));
        processor.setJWSKeySelector(keySelector);

        return processor;
    }
}