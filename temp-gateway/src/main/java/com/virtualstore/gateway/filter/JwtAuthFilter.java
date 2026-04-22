package com.virtualstore.gateway.filter;

import com.virtualstore.gateway.util.JwtUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JwtAuthFilter
 *
 * A Spring Cloud Gateway GlobalFilter that intercepts every request AFTER
 * Spring Security has already validated the JWT.
 *
 * Responsibilities:
 * 1. Read the Authorization: Bearer <token> header.
 * 2. Extract the userId claim via JwtUtility.
 * 3. Inject the userId as X-User-Id header into the downstream request.
 * 4. Strip the original Authorization header from downstream if desired
 * (uncomment stripping logic below — depends on whether services need it).
 *
 * Ordering: Ordered.HIGHEST_PRECEDENCE + 10 ensures this runs very early
 * in the filter chain, just after the built-in security filters.
 *
 * Flow:
 * Browser → [SecurityWebFilter validates JWT] → [JwtAuthFilter adds X-User-Id]
 * → [Route filters] → Downstream service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /** Header name injected into downstream requests */
    public static final String USER_ID_HEADER = "X-User-Id";

    /** Header added to track request correlation across services */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final JwtUtility jwtUtility;

    // ─────────────────────────────────────────────────────────────────────────
    // Filter Logic
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // ── Step 1: Read Authorization header ────────────────────────────────
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No JWT present — let the security layer handle rejection
            // Public paths (like /auth/**) will pass through
            log.debug("No Bearer token found for path: {}", request.getPath());
            return chain.filter(exchange);
        }

        // ── Step 2: Extract raw token ─────────────────────────────────────────
        return jwtUtility.extractBearerToken(authHeader)
                .map(token -> {
                    // ── Step 3: Parse claims & extract userId ─────────────────────
                    String userId = jwtUtility.extractUserId(token)
                            .orElse(null);

                    if (userId == null) {
                        log.warn("Could not extract userId from token for path: {}",
                                request.getPath());
                        return exchange;
                    }

                    log.debug("Injecting X-User-Id: {} for path: {}",
                            userId, request.getPath());

                    // ── Step 4: Mutate request — add X-User-Id header ─────────────
                    ServerHttpRequest mutatedRequest = request.mutate()
                            // Inject userId so downstream services don't re-parse JWT
                            .header(USER_ID_HEADER, userId)
                            // Inject a correlation ID for distributed tracing
                            // (use existing or generate a new one)
                            .header(CORRELATION_ID_HEADER,
                                    getOrGenerateCorrelationId(request))
                            .build();

                    return exchange.mutate().request(mutatedRequest).build();
                })
                // ── Step 5: Continue filter chain with mutated exchange ───────────
                .map(chain::filter)
                .orElseGet(() -> chain.filter(exchange));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ordering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Run this filter very early — after built-in security (which runs at
     * Integer.MIN_VALUE) but before route-specific filters.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns an existing X-Correlation-Id from the request, or generates a
     * new UUID-based correlation ID for distributed tracing.
     */
    private String getOrGenerateCorrelationId(ServerHttpRequest request) {
        String existing = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (existing != null && !existing.isBlank()) {
            return existing;
        }
        return java.util.UUID.randomUUID().toString();
    }
}