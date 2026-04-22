"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.verifyToken = verifyToken;
exports.extractUserId = extractUserId;
exports.extractBearerToken = extractBearerToken;
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const env_js_1 = __importDefault(require("../config/env.js"));
function verifyToken(token) {
    try {
        const payload = jsonwebtoken_1.default.verify(token, env_js_1.default.jwt.secret, {
            algorithms: ["HS256"],
        });
        return { ok: true, payload };
    }
    catch (err) {
        const error = err instanceof Error ? err.message : "Unknown error";
        return { ok: false, error };
    }
}
function extractUserId(payload) {
    if (typeof payload === "string")
        return null;
    const claim = env_js_1.default.jwt.userIdClaim;
    const value = payload[claim];
    if (value === undefined || value === null)
        return null;
    return String(value);
}
function extractBearerToken(authHeader) {
    if (!authHeader || !authHeader.startsWith("Bearer "))
        return null;
    const token = authHeader.slice(7).trim();
    return token.length > 0 ? token : null;
}
//# sourceMappingURL=jwtUtility.js.map