/**
 * middleware/userIdMiddleware.js
 *
 * Reads the decoded JWT payload (set by authMiddleware) and injects the
 * userId as an X-User-Id header so downstream services don't need to
 * parse the JWT themselves.
 *
 * Also injects X-Correlation-Id for distributed tracing — either reads
 * an existing one from the incoming request or generates a new UUID.
 *
 * Must run AFTER authMiddleware (relies on req.jwtPayload being set).
 *
 * These headers are added to req before express-http-proxy forwards it,
 * because proxy is configured to decorate the proxyReq with req.headers.
 */

import { v4 as uuidv4 } from "uuid";
import { extractUserId } from "../utils/jwtUtility.js";
import { Request, Response, NextFunction } from "express";

export const USER_ID_HEADER = "X-User-Id";
export const CORRELATION_ID_HEADER = "X-Correlation-Id";

/**
 * userIdMiddleware
 *
 * Mutates req.headers so the proxy layer forwards them downstream.
 */
export function userIdMiddleware(req: Request, _res: Response, next: NextFunction) {
    const userId = extractUserId(req.jwtPayload ?? {});

    if (userId) {
        req.headers[USER_ID_HEADER] = userId;
    } else {
        console.warn(`[userIdMiddleware] userId claim not found in JWT for ${req.path}`);
    }

    if (!req.headers[CORRELATION_ID_HEADER]) {
        req.headers[CORRELATION_ID_HEADER] = uuidv4();
    }

    next();
}