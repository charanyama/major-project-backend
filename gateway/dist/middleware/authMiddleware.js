"use strict";
/**
 * middleware/authMiddleware.ts
 *
 * Express middleware that validates JWT on protected routes.
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.authMiddleware = authMiddleware;
const jwtUtility_js_1 = require("../utils/jwtUtility.js");
function authMiddleware(req, res, next) {
    // Step 1: Extract token
    const token = (0, jwtUtility_js_1.extractBearerToken)(req.headers.authorization);
    if (!token) {
        res.status(401).json({
            timestamp: new Date().toISOString(),
            status: 401,
            error: "Unauthorized",
            message: "Authentication required. Provide a valid Bearer token.",
            path: req.originalUrl,
        });
        return;
    }
    // Step 2: Verify token
    const result = (0, jwtUtility_js_1.verifyToken)(token);
    if (!result.ok) {
        res.status(401).json({
            timestamp: new Date().toISOString(),
            status: 401,
            error: "Unauthorized",
            message: `Invalid token: ${result.error}`,
            path: req.originalUrl,
        });
        return;
    }
    // Step 3: Attach decoded payload
    req.jwtPayload = result.payload;
    req.rawToken = token;
    next();
}
//# sourceMappingURL=authMiddleware.js.map