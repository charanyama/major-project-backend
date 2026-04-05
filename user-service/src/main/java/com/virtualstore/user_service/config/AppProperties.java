package com.virtualstore.user_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AppProperties — binds the "app" prefix from application.yml.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String frontendUrl;
    private long verificationTokenExpiryMs;
    private long passwordResetExpiryMs;
    private Mail mail = new Mail();

    @Getter
    @Setter
    public static class Mail {
        private String from;
        private String fromName;
    }
}