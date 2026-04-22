import { pool } from "../db";
import { DBEvent } from "../types/event";

export class BatchQueue {
    private queue: DBEvent[] = [];

    constructor(
        private batchSize: number,
        private flushInterval: number
    ) {
        setInterval(() => this.flush(), this.flushInterval);
    }

    add(event: DBEvent) {
        this.queue.push(event);

        if (this.queue.length >= this.batchSize) {
            this.flush();
        }
    }

    private async flush() {
        if (this.queue.length === 0) return;

        const eventsToInsert = this.queue;
        this.queue = [];

        const values: any[] = [];
        const placeholders: string[] = [];

        eventsToInsert.forEach((e, i) => {
            const base = i * 6;

            placeholders.push(
                `($${base + 1}, $${base + 2}, $${base + 3}, $${base + 4}, $${base + 5}, $${base + 6})`
            );

            values.push(
                e.event_type,
                e.user_id,
                e.session_id,
                e.product_id,
                e.timestamp,
                e.metadata
            );
        });

        const query = `
      INSERT INTO events 
      (event_type, user_id, session_id, product_id, timestamp, metadata)
      VALUES ${placeholders.join(",")}
    `;

        try {
            await pool.query(query, values);
        } catch (err) {
            console.error("Batch insert failed:", err);
        }
    }
}