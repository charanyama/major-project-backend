/**
 * controllers/dashboardController.ts
 *
 * BFF orchestration layer (TypeScript version)
 */

import { Request, Response, NextFunction } from "express";
import { productClient, orderClient } from "../utils/httpClient.js";
import { USER_ID_HEADER } from "../middleware/userIdMiddleware.js";

type ProductSummary = {
    totalProducts: number | null;
    featuredProducts: any[];
    error?: string;
};

type OrderSummary = {
    totalOrders: number | null;
    pendingOrders: number | null;
    recentOrders: any[];
    error?: string;
};

type DashboardResponse = {
    generatedAt: string;
    userId: string;
    partialFailure: boolean;
    products: ProductSummary | { error: string };
    orders: OrderSummary | { error: string };
};

type Settled<T> =
    | { status: "fulfilled"; value: T }
    | { status: "rejected"; reason: any };

export async function getDashboard(
    req: Request,
    res: Response,
    next: NextFunction
): Promise<void> {
    try {
        const userId =
            (req.headers[USER_ID_HEADER] as string) ??
            (req.jwtPayload as any)?.sub ??
            "unknown";

        const [productsResult, ordersResult] = await Promise.allSettled([
            fetchProductSummary(userId),
            fetchOrderSummary(userId),
        ]);

        const products = resolveResult<ProductSummary>(productsResult, "products");
        const orders = resolveResult<OrderSummary>(ordersResult, "orders");

        const partialFailure =
            "error" in products || "error" in orders;

        const response: DashboardResponse = {
            generatedAt: new Date().toISOString(),
            userId,
            partialFailure,
            products,
            orders,
        };

        res.status(200).json(response);
    } catch (err) {
        next(err);
    }
}

async function fetchProductSummary(userId: string): Promise<ProductSummary> {
    const { data } = await productClient.get("/api/products/summary", {
        headers: { [USER_ID_HEADER]: userId },
    });

    return {
        totalProducts: data.totalProducts ?? null,
        featuredProducts: data.featuredProducts ?? [],
    };
}

async function fetchOrderSummary(userId: string): Promise<OrderSummary> {
    const { data } = await orderClient.get("/api/orders/summary", {
        headers: { [USER_ID_HEADER]: userId },
    });

    return {
        totalOrders: data.totalOrders ?? null,
        pendingOrders: data.pendingOrders ?? null,
        recentOrders: data.recentOrders ?? [],
    };
}

function resolveResult<T>(
    settled: PromiseSettledResult<T>,
    label: string
): T | { error: string } {
    if (settled.status === "fulfilled") {
        return settled.value;
    }

    const reason: any = settled.reason;
    const httpStatus = reason?.response?.status;

    console.warn(
        `[dashboard] ${label} call failed: ${reason?.message} (HTTP ${httpStatus ?? "no response"
        })`
    );

    return {
        error: httpStatus
            ? `Service returned HTTP ${httpStatus}`
            : "Service temporarily unavailable",
    } as any;
}