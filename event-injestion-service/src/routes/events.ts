import { Router, Request, Response } from "express";
import { ingestEvent } from "../services/eventService";
import { v4 as uuidv4 } from "uuid";

const router = Router();

router.post("/", async (req: Request, res: Response) => {
    try {
        const {
            eventType,
            userId = null,
            productId = null,
            sessionId = uuidv4(),
            metadata = {},
        } = req.body;

        const event = {
            event_type: eventType,
            user_id: userId,
            product_id: productId,
            session_id: sessionId,
            timestamp: new Date(),
            metadata,
        };

        // Fire-and-forget
        ingestEvent(event).catch(console.error);

        res.status(202).json({ status: "accepted" });
    } catch (err) {
        res.status(500).json({ error: "internal_error" });
    }
});

export default router;