"use strict";
/**
 * middleware/errorMiddleware.ts
 *
 * Express global error + 404 handlers
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.errorHandler = void 0;
exports.notFoundHandler = notFoundHandler;
function notFoundHandler(req, res) {
    res.status(404).json({
        timestamp: new Date().toISOString(),
        status: 404,
        error: "Not Found",
        message: `No route found for ${req.method} ${req.originalUrl}`,
        path: req.originalUrl,
    });
}
const errorHandler = (err, req, res, _next) => {
    console.error(`[errorHandler] ${err.message}`, err.stack);
    const status = err.status ?? err.statusCode ?? 500;
    res.status(status).json({
        timestamp: new Date().toISOString(),
        status,
        error: statusLabel(status),
        message: status < 500
            ? err.message
            : "An unexpected error occurred. Please try again later.",
        path: req.originalUrl,
    });
};
exports.errorHandler = errorHandler;
function statusLabel(status) {
    const labels = {
        400: "Bad Request",
        401: "Unauthorized",
        403: "Forbidden",
        404: "Not Found",
        502: "Bad Gateway",
        503: "Service Unavailable",
    };
    return labels[status] ?? "Internal Server Error";
}
//# sourceMappingURL=errorMiddleware.js.map