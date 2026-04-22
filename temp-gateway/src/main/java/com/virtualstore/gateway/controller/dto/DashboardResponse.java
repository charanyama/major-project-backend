package com.virtualstore.gateway.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DashboardResponse
 *
 * Aggregated BFF response for GET /api/dashboard.
 * Combines data from Product Service and Order Service into a single payload.
 *
 * @JsonInclude(NON_NULL): fields absent due to partial failures are omitted
 * instead of being serialized as null.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {

    /** ISO-8601 timestamp of when this response was assembled */
    private String generatedAt;

    /** userId extracted from JWT (echoed back for the frontend) */
    private String userId;

    /** Summary block from Product Service */
    private ProductSummary products;

    /** Summary block from Order Service */
    private OrderSummary orders;

    /**
     * Indicates whether any downstream call failed.
     * Frontend can use this to show a partial-data warning.
     */
    private boolean partialFailure;

    // ─────────────────────────────────────────────────────────────────────────
    // Nested DTOs — one per downstream service
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Product Service summary.
     * Extend this with actual fields returned by your Product Service.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProductSummary {
        /** Total active product count */
        private Integer totalProducts;
        /** Top featured products (preview) */
        private List<Object> featuredProducts;
        /** Error message if the product-service call failed */
        private String error;
    }

    /**
     * Order Service summary.
     * Extend this with actual fields returned by your Order Service.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderSummary {
        /** Total orders for this user */
        private Integer totalOrders;
        /** Count of orders in PENDING state */
        private Integer pendingOrders;
        /** Recent orders list (preview) */
        private List<Object> recentOrders;
        /** Error message if the order-service call failed */
        private String error;
    }
}