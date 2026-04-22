"use strict";
/**
 * controllers/dashboardController.ts
 *
 * BFF orchestration layer (TypeScript version)
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.getDashboard = getDashboard;
const httpClient_js_1 = require("../utils/httpClient.js");
const userIdMiddleware_js_1 = require("../middleware/userIdMiddleware.js");
async function getDashboard(req, res, next) {
    try {
        const userId = req.headers[userIdMiddleware_js_1.USER_ID_HEADER] ??
            req.jwtPayload?.sub ??
            "unknown";
        const [productsResult, ordersResult] = await Promise.allSettled([
            fetchProductSummary(userId),
            fetchOrderSummary(userId),
        ]);
        const products = resolveResult(productsResult, "products");
        const orders = resolveResult(ordersResult, "orders");
        const partialFailure = "error" in products || "error" in orders;
        const response = {
            generatedAt: new Date().toISOString(),
            userId,
            partialFailure,
            products,
            orders,
        };
        res.status(200).json(response);
    }
    catch (err) {
        next(err);
    }
}
async function fetchProductSummary(userId) {
    const { data } = await httpClient_js_1.productClient.get("/api/products/summary", {
        headers: { [userIdMiddleware_js_1.USER_ID_HEADER]: userId },
    });
    return {
        totalProducts: data.totalProducts ?? null,
        featuredProducts: data.featuredProducts ?? [],
    };
}
async function fetchOrderSummary(userId) {
    const { data } = await httpClient_js_1.orderClient.get("/api/orders/summary", {
        headers: { [userIdMiddleware_js_1.USER_ID_HEADER]: userId },
    });
    return {
        totalOrders: data.totalOrders ?? null,
        pendingOrders: data.pendingOrders ?? null,
        recentOrders: data.recentOrders ?? [],
    };
}
function resolveResult(settled, label) {
    if (settled.status === "fulfilled") {
        return settled.value;
    }
    const reason = settled.reason;
    const httpStatus = reason?.response?.status;
    console.warn(`[dashboard] ${label} call failed: ${reason?.message} (HTTP ${httpStatus ?? "no response"})`);
    return {
        error: httpStatus
            ? `Service returned HTTP ${httpStatus}`
            : "Service temporarily unavailable",
    };
}
//# sourceMappingURL=dashboard.js.map