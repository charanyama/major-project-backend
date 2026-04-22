"use strict";
/**
 * routes/index.ts
 *
 * Central routing configuration for the API Gateway
 */
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const express_http_proxy_1 = __importDefault(require("express-http-proxy"));
const env_js_1 = __importDefault(require("../config/env.js"));
const authMiddleware_js_1 = require("../middleware/authMiddleware.js");
const userIdMiddleware_js_1 = require("../middleware/userIdMiddleware.js");
const dashboard_js_1 = require("../controllers/dashboard.js");
const router = (0, express_1.Router)();
function proxyTo(targetBaseUrl) {
    return (0, express_http_proxy_1.default)(targetBaseUrl, {
        proxyReqPathResolver: (req) => req.originalUrl,
        proxyReqOptDecorator: (proxyReqOpts, srcReq) => {
            proxyReqOpts.headers = {
                ...proxyReqOpts.headers,
                ...srcReq.headers,
            };
            return proxyReqOpts;
        },
        userResDecorator: (_proxyRes, proxyResData) => {
            return proxyResData;
        },
        timeout: 15_000,
        proxyErrorHandler: (err, res, next) => {
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
const protect = [authMiddleware_js_1.authMiddleware, userIdMiddleware_js_1.userIdMiddleware];
router.use("/auth", proxyTo(env_js_1.default.services.user));
router.get("/api/dashboard", ...protect, dashboard_js_1.getDashboard);
router.use("/api/users", ...protect, proxyTo(env_js_1.default.services.user));
router.use("/api/products", ...protect, proxyTo(env_js_1.default.services.product));
router.use("/api/orders", ...protect, proxyTo(env_js_1.default.services.order));
exports.default = router;
//# sourceMappingURL=index.js.map