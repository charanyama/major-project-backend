package com.virtualstore.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.session.WebSessionManager;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.InMemoryWebSessionStore;

/**
 * SessionConfig
 *
 * Provides session management infrastructure.
 *
 * PRIMARY mode (default): Stateless JWT — no server-side sessions.
 * The InMemoryWebSessionStore is a no-op placeholder.
 *
 * REDIS mode (optional): Enable by setting app.session.store=redis in
 * application.yml.
 * Uncomment @EnableRedisWebSession and the SpringSessionWebSessionStore bean.
 * Requires spring-session-data-redis on the classpath and Redis running.
 *
 * To switch to Redis sessions:
 * 1. Uncomment @EnableRedisWebSession below
 * 2. Ensure Redis is configured in application.yml (spring.data.redis.*)
 * 3. Uncomment the redisSessionManager() bean
 * 4. Comment out the inMemorySessionManager() bean
 */
@Configuration
public class SessionConfig {

    /**
     * Default WebSessionManager using in-memory storage.
     * Used when Redis is not configured.
     *
     * Active when: app.session.store != redis (default)
     */
    @Bean
    @ConditionalOnProperty(
        name = "app.session.store",
        havingValue = "memory", 
        matchIfMissing = true // Default if property not set
    )
    public WebSessionManager inMemorySessionManager() {
        DefaultWebSessionManager manager = new DefaultWebSessionManager();
        manager.setSessionStore(new InMemoryWebSessionStore());
        return manager;
    }
}