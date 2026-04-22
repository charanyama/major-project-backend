"use strict";
/**
 * server.ts
 *
 * Entry point. Starts HTTP server from createApp().
 */
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const app_js_1 = require("./app.js");
const env_js_1 = __importDefault(require("./config/env.js"));
const app = (0, app_js_1.createApp)();
const server = app.listen(env_js_1.default.port, () => {
    console.log(`
╔══════════════════════════════════════════════════╗
║          Ecommerce API Gateway                   ║
║──────────────────────────────────────────────────║
║  Port     : ${String(env_js_1.default.port).padEnd(35)}     ║
║  Env      : ${env_js_1.default.nodeEnv.padEnd(35)}          ║
║──────────────────────────────────────────────────║
║  Routes:                                         ║
║  /auth/**         → User Service (public)        ║
║  /api/users/**    → User Service (JWT)           ║
║  /api/products/** → Product Service (JWT)        ║
║  /api/orders/**   → Order Service (JWT)          ║
║  /api/dashboard   → BFF Aggregation (JWT)        ║
╚══════════════════════════════════════════════════╝
`);
});
function shutdown(signal) {
    console.log(`\n[gateway] Received ${signal}. Shutting down gracefully...`);
    server.close(() => {
        console.log("[gateway] HTTP server closed.");
        process.exit(0);
    });
    setTimeout(() => {
        console.error("[gateway] Forced exit after timeout.");
        process.exit(1);
    }, 10_000);
}
process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));
//# sourceMappingURL=server.js.map