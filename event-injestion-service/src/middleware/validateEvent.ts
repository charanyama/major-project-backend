import { Request, Response, NextFunction } from "express";
import { EventPayload } from "../types/event";

export const validateEvent = (req: Request<{}, {}, EventPayload>, res: Response, next: NextFunction,) => {
    if (!req.body.eventType) {
        return res.status(400).json({ error: "eventType is required" });
    }

    next();
};