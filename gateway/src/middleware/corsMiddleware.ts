/**
 * middleware/corsMiddleware.js
 *
 * Manual CORS middleware — avoids the `cors` npm package dependency.
 * Reads allowed origins from config (CORS_ORIGINS env var).
 *
 * Handles:
 *   - Preflight OPTIONS requests (returns 204 immediately)
 *   - Origin allowlist check per request
 *   - Credential support for cookie-based flows (if ever needed)
 */

import { Request, Response, NextFunction } from "express";
import config from "../config/env.js";

export function corsMiddleware(req: Request, res: Response, next: NextFunction) {
    const origin = req.headers.origin;

    if (origin && config.corsOrigins.includes(origin)) {
        res.setHeader("Access-Control-Allow-Origin", origin);
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }

    res.setHeader(
        "Access-Control-Allow-Methods",
        "GET, POST, PUT, PATCH, DELETE, OPTIONS"
    );

    res.setHeader(
        "Access-Control-Allow-Headers",
        "Content-Type, Authorization, X-User-Id, X-Correlation-Id"
    );
    
    res.setHeader("Access-Control-Max-Age", "3600");

    if (req.method === "OPTIONS") {
        return res.sendStatus(204);
    }

    next();
}