/**
 * middleware/errorMiddleware.ts
 *
 * Express global error + 404 handlers
 */

import { Request, Response, NextFunction, ErrorRequestHandler } from "express";

export function notFoundHandler(req: Request, res: Response): void {
    res.status(404).json({
        timestamp: new Date().toISOString(),
        status: 404,
        error: "Not Found",
        message: `No route found for ${req.method} ${req.originalUrl}`,
        path: req.originalUrl,
    });
}

type AppError = Error & {
    status?: number;
    statusCode?: number;
};

export const errorHandler: ErrorRequestHandler = (
    err: AppError,
    req: Request,
    res: Response,
    _next: NextFunction
): void => {
    console.error(`[errorHandler] ${err.message}`, err.stack);

    const status = err.status ?? err.statusCode ?? 500;

    res.status(status).json({
        timestamp: new Date().toISOString(),
        status,
        error: statusLabel(status),
        message:
            status < 500
                ? err.message
                : "An unexpected error occurred. Please try again later.",
        path: req.originalUrl,
    });
};

function statusLabel(status: number): string {
    const labels: Record<number, string> = {
        400: "Bad Request",
        401: "Unauthorized",
        403: "Forbidden",
        404: "Not Found",
        502: "Bad Gateway",
        503: "Service Unavailable",
    };

    return labels[status] ?? "Internal Server Error";
}