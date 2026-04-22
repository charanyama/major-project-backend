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

import "dotenv/config";
import express from "express";
import morgan from "morgan";

import config from "./config/env.js";
import { corsMiddleware } from "./middleware/corsMiddleware.js";
import { notFoundHandler, errorHandler } from "./middleware/errorMiddleware.js";
import routes from "./routes/index.js";

export function createApp() {
    const app = express();

    app.use(corsMiddleware);

    app.use(express.json({ limit: "1mb" }));
    app.use(express.urlencoded({ extended: true }));

    app.use(morgan(config.logFormat));

    app.get("/actuator/health", (_req, res) => {
        res.json({ status: "UP", service: "api-gateway" });
    });

    app.use(routes);

    app.use(notFoundHandler);

    app.use(errorHandler);

    return app;
}