package com.virtualstore.gateway.config;

import com.virtualstore.gateway.util.JwtUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * SecurityConfig
 *
 * Defines the reactive security filter chain for the API Gateway.
 *
 * Key decisions:
 * - CSRF disabled → Stateless JWT; no session cookies.
 * - CORS handled → Spring Cloud Gateway globalcors config in application.yml.
 * - /auth/** → Public (no JWT required).
 * - /actuator/** → Public (lock down in production).
 * - Everything else → Must carry a valid Bearer JWT.
 *
 * JWT is decoded using the symmetric secret key (HS256) via JwtUtility.
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtility jwtUtility;

    /**
     * Main SecurityWebFilterChain bean.
     *
     * NOTE: We use NoOpServerSecurityContextRepository to enforce statelessness —
     * no session is stored on the server side.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                // ── Stateless: no server-side security context storage ──────────
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // ── CSRF disabled (stateless JWT, no cookie-based auth) ─────────
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // ── Authorization rules ─────────────────────────────────────────
                .authorizeExchange(exchanges -> exchanges

                        // Public: Auth endpoints (login, register, token refresh)
                        .pathMatchers("/auth/**").permitAll()

                        // Public: Actuator health check (restrict others in prod)
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Public: OPTIONS preflight (CORS)
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Protected: Everything else requires a valid JWT
                        .anyExchange().authenticated())

                // ── OAuth2 Resource Server: JWT decoder ─────────────────────────
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(reactiveJwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))

                .build();
    }

    /**
     * ReactiveJwtDecoder
     *
     * Uses the symmetric secret key (HS256) from JwtUtility.
     * For RS256/asymmetric, replace with:
     * NimbusReactiveJwtDecoder.withJwkSetUri(jwksUri).build()
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        // JwtUtility creates a SecretKeySpec from the configured secret
        return NimbusReactiveJwtDecoder
                .withSecretKey(jwtUtility.getSecretKey())
                .build();
    }

    /**
     * JWT → Authentication converter.
     *
     * Extracts granted authorities from the "roles" claim (or "scope").
     * Prefix ROLE_ is applied automatically by JwtGrantedAuthoritiesConverter.
     */
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Map the "roles" claim in JWT to Spring Security authorities
        authoritiesConverter.setAuthoritiesClaimName("roles");
        // Prefix for roles (ROLE_ADMIN, ROLE_USER, etc.)
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}