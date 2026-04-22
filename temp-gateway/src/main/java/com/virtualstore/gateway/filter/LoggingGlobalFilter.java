package com.virtualstore.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * LoggingGlobalFilter
 *
 * Logs every inbound request and outbound response for observability.
 * Runs BEFORE JwtAuthFilter to capture the original request details.
 *
 * Log format:
 * → [METHOD] /path (request start)
 * ← HTTP 200 in 45ms (after response)
 *
 * In production, replace with a structured logger (JSON) and ship to
 * Loki / Datadog / CloudWatch.
 */
@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().toString();

        log.info("→ {} {}", method, path);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long durationMs = System.currentTimeMillis() - startTime;
                    ServerHttpResponse response = exchange.getResponse();
                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value()
                            : 0;
                    log.info("← HTTP {} in {}ms [{} {}]",
                            statusCode, durationMs, method, path);
                });
    }

    /**
     * Run before JwtAuthFilter (which is HIGHEST_PRECEDENCE + 100)
     * so we log before any header mutation occurs.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}