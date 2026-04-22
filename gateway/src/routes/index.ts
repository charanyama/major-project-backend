/**
 * routes/index.ts
 *
 * Central routing configuration for the API Gateway
 */

import { Router, Request, Response, NextFunction } from "express";
import proxy from "express-http-proxy";

import config from "../config/env.js";
import { authMiddleware } from "../middleware/authMiddleware.js";
import { userIdMiddleware } from "../middleware/userIdMiddleware.js";
import { getDashboard } from "../controllers/dashboard.js";

const router = Router();

function proxyTo(targetBaseUrl: string) {
    return proxy(targetBaseUrl, {
        proxyReqPathResolver: (req: Request) => req.originalUrl,

        proxyReqOptDecorator: (proxyReqOpts: any, srcReq: Request) => {
            proxyReqOpts.headers = {
                ...proxyReqOpts.headers,
                ...srcReq.headers,
            };
            return proxyReqOpts;
        },

        userResDecorator: (_proxyRes: any, proxyResData: any) => {
            return proxyResData;
        },

        timeout: 15_000,

        proxyErrorHandler: (err: any, res: Response, next: NextFunction) => {
            if (err.code === "ECONNREFUSED" || err.code === "ENOTFOUND") {
                res.status(503).json({
                    timestamp: new Date().toISOString(),
                    status: 503,
                    error: "Service Unavailable",
                    message: "A downstream service is currently unreachable.",
                });
                return;
            }

            next(err);
        },
    });
}

const protect = [authMiddleware, userIdMiddleware];

router.use("/auth", proxyTo(config.services.user));

router.get("/api/dashboard", ...protect, getDashboard);

router.use("/api/users", ...protect, proxyTo(config.services.user));

router.use("/api/products", ...protect, proxyTo(config.services.product));

router.use("/api/orders", ...protect, proxyTo(config.services.order));

export default router;