"use strict";
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
Object.defineProperty(exports, "__esModule", { value: true });
exports.CORRELATION_ID_HEADER = exports.USER_ID_HEADER = void 0;
exports.userIdMiddleware = userIdMiddleware;
const uuid_1 = require("uuid");
const jwtUtility_js_1 = require("../utils/jwtUtility.js");
exports.USER_ID_HEADER = "X-User-Id";
exports.CORRELATION_ID_HEADER = "X-Correlation-Id";
/**
 * userIdMiddleware
 *
 * Mutates req.headers so the proxy layer forwards them downstream.
 */
function userIdMiddleware(req, _res, next) {
    const userId = (0, jwtUtility_js_1.extractUserId)(req.jwtPayload ?? {});
    if (userId) {
        req.headers[exports.USER_ID_HEADER] = userId;
    }
    else {
        console.warn(`[userIdMiddleware] userId claim not found in JWT for ${req.path}`);
    }
    if (!req.headers[exports.CORRELATION_ID_HEADER]) {
        req.headers[exports.CORRELATION_ID_HEADER] = (0, uuid_1.v4)();
    }
    next();
}
//# sourceMappingURL=userIdMiddleware.js.map