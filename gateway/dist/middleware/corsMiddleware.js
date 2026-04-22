"use strict";
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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.corsMiddleware = corsMiddleware;
const env_js_1 = __importDefault(require("../config/env.js"));
function corsMiddleware(req, res, next) {
    const origin = req.headers.origin;
    if (origin && env_js_1.default.corsOrigins.includes(origin)) {
        res.setHeader("Access-Control-Allow-Origin", origin);
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-User-Id, X-Correlation-Id");
    res.setHeader("Access-Control-Max-Age", "3600");
    if (req.method === "OPTIONS") {
        return res.sendStatus(204);
    }
    next();
}
//# sourceMappingURL=corsMiddleware.js.map