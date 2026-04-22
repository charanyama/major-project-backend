package com.virtualstore.user_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AppProperties — binds the "app" prefix from application.yml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String frontendUrl;
    private long passwordResetExpiryMs;
    private long emailVerificationExpiryMs;
    private Mail mail = new Mail();

    @Getter
    @Setter
    public static class Mail {
        private String username;
        private String password;
        private String from;
        private String fromName;
    }
}
