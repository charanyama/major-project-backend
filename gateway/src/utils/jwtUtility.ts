import jwt, { JwtPayload } from "jsonwebtoken";
import config from "../config/env.js";

type VerifySuccess = {
    ok: true;
    payload: JwtPayload | string;
};

type VerifyFailure = {
    ok: false;
    error: string;
};

type VerifyResult = VerifySuccess | VerifyFailure;

export function verifyToken(token: string): VerifyResult {
    try {
        const payload = jwt.verify(token, config.jwt.secret, {
            algorithms: ["HS256"],
        });

        return { ok: true, payload };
    } catch (err) {
        const error = err instanceof Error ? err.message : "Unknown error";
        return { ok: false, error };
    }
}

export function extractUserId(payload: JwtPayload | string): string | null {
    if (typeof payload === "string") return null;

    const claim = config.jwt.userIdClaim;
    const value = payload[claim];

    if (value === undefined || value === null) return null;

    return String(value);
}

export function extractBearerToken(authHeader?: string): string | null {
    if (!authHeader || !authHeader.startsWith("Bearer ")) return null;

    const token = authHeader.slice(7).trim();
    return token.length > 0 ? token : null;
} 