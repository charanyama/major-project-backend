/**
 * middleware/authMiddleware.ts
 *
 * Express middleware that validates JWT on protected routes.
 */

import { Request, Response, NextFunction } from "express";
import { verifyToken, extractBearerToken } from "../utils/jwtUtility.js";
import { JwtPayload } from "jsonwebtoken";

declare global {
  namespace Express {
    interface Request {
      jwtPayload?: JwtPayload | string;
      rawToken?: string;
    }
  }
}

export function authMiddleware(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  // Step 1: Extract token
  const token = extractBearerToken(req.headers.authorization);

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
  const result = verifyToken(token);

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