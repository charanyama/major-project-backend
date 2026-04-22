package com.virtualstore.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GatewayProperties
 *
 * Binds strongly-typed configuration from application.yml.
 * Prefix: "services" → downstream microservice base URLs.
 *
 * Usage in application.yml:
 * services:
 * product-service:
 * base-url: http://localhost:8082
 * order-service:
 * base-url: http://localhost:8083
 * user-service:
 * base-url: http://localhost:8081
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "services")
public class GatewayProperties {

    private ServiceConfig productService = new ServiceConfig();
    private ServiceConfig orderService = new ServiceConfig();
    private ServiceConfig userService = new ServiceConfig();

    @Getter
    @Setter
    public static class ServiceConfig {
        /** Base URL for the microservice, e.g. http://localhost:8082 */
        private String baseUrl;
    }
}