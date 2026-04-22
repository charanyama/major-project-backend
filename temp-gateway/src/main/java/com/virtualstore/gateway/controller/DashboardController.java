package com.virtualstore.gateway.controller;

import com.virtualstore.gateway.controller.dto.DashboardResponse;
import com.virtualstore.gateway.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DashboardController
 *
 * BFF (Backend for Frontend) orchestration layer.
 *
 * GET /api/dashboard
 * - Calls Product Service: GET /api/products/summary
 * - Calls Order Service: GET /api/orders/summary
 * - Aggregates both responses into a single DashboardResponse
 * - Uses reactive parallel execution (Mono.zip) — not sequential
 * - Handles partial failures gracefully (one service down ≠ full failure)
 *
 * JWT Principal:
 * - @AuthenticationPrincipal Jwt gives us the validated token object
 * - We extract userId from it (no second JWT parsing needed)
 *
 * Downstream headers:
 * - X-User-Id is forwarded so services can filter user-specific data
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final @Qualifier("productServiceClient") WebClient productServiceClient;
    private final @Qualifier("orderServiceClient") WebClient orderServiceClient;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/dashboard
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Aggregates product and order summaries for the authenticated user.
     *
     * @param jwt      Validated JWT principal injected by Spring Security
     * @param exchange ServerWebExchange — used to read X-User-Id header set by
     *                 JwtAuthFilter
     */
    @GetMapping
    public Mono<ResponseEntity<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal Jwt jwt,
            ServerWebExchange exchange) {

        // Extract userId — prefer the header set by JwtAuthFilter, fall back to JWT sub
        String userId = exchange.getRequest()
                .getHeaders()
                .getFirst(JwtAuthFilter.USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            userId = jwt.getSubject();
        }

        final String resolvedUserId = userId;
        log.debug("Dashboard request for userId: {}", resolvedUserId);

        // ── Parallel calls to Product and Order services ───────────────────────
        //
        // Mono.zip executes both calls concurrently on the reactive scheduler.
        // If one fails, onErrorResume catches it so the other still returns.

        Mono<DashboardResponse.ProductSummary> productsMono = fetchProductSummary(resolvedUserId);

        Mono<DashboardResponse.OrderSummary> ordersMono = fetchOrderSummary(resolvedUserId);

        return Mono.zip(productsMono, ordersMono)
                .map(tuple -> {
                    DashboardResponse.ProductSummary products = tuple.getT1();
                    DashboardResponse.OrderSummary orders = tuple.getT2();

                    boolean partialFailure = products.getError() != null
                            || orders.getError() != null;

                    DashboardResponse response = DashboardResponse.builder()
                            .generatedAt(Instant.now().toString())
                            .userId(resolvedUserId)
                            .products(products)
                            .orders(orders)
                            .partialFailure(partialFailure)
                            .build();

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(ex -> {
                    log.error("Dashboard aggregation failed entirely: {}", ex.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(DashboardResponse.builder()
                                    .generatedAt(Instant.now().toString())
                                    .userId(resolvedUserId)
                                    .partialFailure(true)
                                    .build()));
                });
    }

    /**
     * Calls GET /api/products/summary on the Product Service.
     *
     * Returns a degraded ProductSummary with an error field if the call fails.
     * This ensures the dashboard still renders with partial data.
     */
    private Mono<DashboardResponse.ProductSummary> fetchProductSummary(String userId) {
        return productServiceClient.get()
                .uri("/api/products/summary")
                .header(JwtAuthFilter.USER_ID_HEADER, userId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> DashboardResponse.ProductSummary.builder()
                        // Safely extract totalProducts; default to 0 if absent
                        .totalProducts(extractInt(body, "totalProducts"))
                        // Safely extract featured products list
                        .featuredProducts(extractList(body, "featuredProducts"))
                        .build())
                .onErrorResume(ex -> {
                    log.warn("Product Service call failed: {}", ex.getMessage());
                    return Mono.just(DashboardResponse.ProductSummary.builder()
                            .error(friendlyError(ex))
                            .build());
                });
    }

    /**
     * Calls GET /api/orders/summary on the Order Service.
     * Passes X-User-Id so Order Service returns only this user's orders.
     */
    private Mono<DashboardResponse.OrderSummary> fetchOrderSummary(String userId) {
        return orderServiceClient.get()
                .uri("/api/orders/summary")
                .header(JwtAuthFilter.USER_ID_HEADER, userId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> DashboardResponse.OrderSummary.builder()
                        .totalOrders(extractInt(body, "totalOrders"))
                        .pendingOrders(extractInt(body, "pendingOrders"))
                        .recentOrders(extractList(body, "recentOrders"))
                        .build())
                .onErrorResume(ex -> {
                    log.warn("Order Service call failed: {}", ex.getMessage());
                    return Mono.just(DashboardResponse.OrderSummary.builder()
                            .error(friendlyError(ex))
                            .build());
                });
    }

    /*
        ! Helpers 
    */
    /** Safely casts a Map value to Integer */
    private Integer extractInt(Map<?, ?> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n)
            return n.intValue();
        return null;
    }

    /** Safely casts a Map value to List */
    private List<Object> extractList(Map<?, ?> map, String key) {
        Object val = map.get(key);
        if (val instanceof List<?> list)
            return list.stream().map(o -> (Object) o).toList();
        return List.of();
    }

    /** Converts exceptions to user-friendly error strings */
    private String friendlyError(Throwable ex) {
        if (ex instanceof WebClientResponseException wcEx) {
            return "Service returned HTTP " + wcEx.getStatusCode().value();
        }
        return "Service temporarily unavailable";
    }
}