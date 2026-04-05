package com.virtualstore.user_service.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JwtProperties — binds the "jwt" prefix from application.yml.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 signing secret — must match the API Gateway's JWT_SECRET */
    private String secret;

    /** Access token lifetime in milliseconds (default 15 min) */
    private long accessTokenExpiryMs;

    /** Refresh token lifetime in milliseconds (default 7 days) */
    private long refreshTokenExpiryMs;
}