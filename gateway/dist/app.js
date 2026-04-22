"use strict";
/**
 * app.js
 *
 * Express application factory.
 * Wires together all middleware and routes but does NOT call server.listen().
 * This separation keeps the app testable — tests import createApp() directly.
 *
 * Middleware order (matters in Express):
 *   1. CORS             — must be first to handle preflight OPTIONS
 *   2. Body parsers     — JSON + URL-encoded
 *   3. Morgan logger    — logs all requests
 *   4. Routes           — business logic
 *   5. 404 handler      — catches unmatched routes
 *   6. Error handler    — catches all thrown/next(err) errors
 */
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.createApp = createApp;
require("dotenv/config");
const express_1 = __importDefault(require("express"));
const morgan_1 = __importDefault(require("morgan"));
const env_js_1 = __importDefault(require("./config/env.js"));
const corsMiddleware_js_1 = require("./middleware/corsMiddleware.js");
const errorMiddleware_js_1 = require("./middleware/errorMiddleware.js");
const index_js_1 = __importDefault(require("./routes/index.js"));
function createApp() {
    const app = (0, express_1.default)();
    app.use(corsMiddleware_js_1.corsMiddleware);
    app.use(express_1.default.json({ limit: "1mb" }));
    app.use(express_1.default.urlencoded({ extended: true }));
    app.use((0, morgan_1.default)(env_js_1.default.logFormat));
    app.get("/actuator/health", (_req, res) => {
        res.json({ status: "UP", service: "api-gateway" });
    });
    app.use(index_js_1.default);
    app.use(errorMiddleware_js_1.notFoundHandler);
    app.use(errorMiddleware_js_1.errorHandler);
    return app;
}
//# sourceMappingURL=app.js.map