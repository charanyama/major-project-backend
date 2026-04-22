package com.virtualstore.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JwtProperties
 *
 * Binds JWT-related configuration from application.yml.
 * Prefix: "jwt"
 *
 * Usage in application.yml:
 * jwt:
 * secret: mySecretKey
 * algorithm: HS256
 * user-id-claim: sub
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Symmetric secret key (for HS256). Injected via env var JWT_SECRET. */
    private String secret;

    /** Signing algorithm: HS256 (symmetric) or RS256 (asymmetric). */
    private String algorithm = "HS256";

    /**
     * Name of the JWT claim that holds the user identifier.
     * Defaults to "sub" (standard subject claim).
     * Override to "userId" or "user_id" based on your auth server config.
     */
    private String userIdClaim = "sub";
}