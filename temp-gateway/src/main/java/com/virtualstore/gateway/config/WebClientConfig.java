package com.virtualstore.gateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClientConfig
 *
 * Provides pre-configured WebClient instances for each downstream microservice.
 * Each client:
 * - Has a fixed base URL (from GatewayProperties)
 * - Enforces connection/read/write timeouts
 * - Logs requests and responses (DEBUG level)
 *
 * The BFF orchestration layer (DashboardController) injects these beans.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final GatewayProperties gatewayProperties;

    // ─────────────────────────────────────────────────────────────────────────
    // Shared Reactor Netty HttpClient with timeouts
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a Reactor Netty HttpClient with sensible timeout defaults.
     * Adjust these values per SLA requirements.
     */
    private HttpClient buildHttpClient() {
        return HttpClient.create()
                // TCP connection timeout: fail fast if service is unreachable
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                // Response timeout: max time waiting for the first byte
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn -> conn
                        // Read timeout: max time between two data packets
                        .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                        // Write timeout: max time to send the request
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-service WebClient beans
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * WebClient for Product Service (port 8082).
     * Named bean "productServiceClient" — injected by DashboardController.
     */
    @Bean("productServiceClient")
    public WebClient productServiceClient() {
        return WebClient.builder()
                .baseUrl(gatewayProperties.getProductService().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
                .filter(loggingFilter("ProductService"))
                .build();
    }

    /**
     * WebClient for Order Service (port 8083).
     */
    @Bean("orderServiceClient")
    public WebClient orderServiceClient() {
        return WebClient.builder()
                .baseUrl(gatewayProperties.getOrderService().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
                .filter(loggingFilter("OrderService"))
                .build();
    }

    /**
     * WebClient for User Service (port 8081).
     */
    @Bean("userServiceClient")
    public WebClient userServiceClient() {
        return WebClient.builder()
                .baseUrl(gatewayProperties.getUserService().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
                .filter(loggingFilter("UserService"))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared logging filter
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * ExchangeFilterFunction that logs outgoing requests and incoming responses.
     * Useful for debugging service-to-service calls.
     *
     * @param serviceName Label for log lines
     */
    private ExchangeFilterFunction loggingFilter(String serviceName) {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("[{}] → {} {}", serviceName, request.method(), request.url());
            return Mono.just(request);
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.debug("[{}] ← HTTP {}", serviceName, response.statusCode());
            return Mono.just(response);
        }));
    }
}