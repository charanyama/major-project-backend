/**
 * server.ts
 *
 * Entry point. Starts HTTP server from createApp().
 */

import { createApp } from "./app.js";
import config from "./config/env.js";
import type { Server } from "http";

const app = createApp();

const server: Server = app.listen(config.port, () => {
    console.log(`
╔══════════════════════════════════════════════════╗
║          Ecommerce API Gateway                   ║
║──────────────────────────────────────────────────║
║  Port     : ${String(config.port).padEnd(35)}     ║
║  Env      : ${config.nodeEnv.padEnd(35)}          ║
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

function shutdown(signal: string): void {
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