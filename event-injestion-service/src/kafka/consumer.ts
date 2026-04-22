import { Kafka } from "kafkajs";
import { pool } from "../db";

const kafka = new Kafka({
    clientId: "event-consumer",
    brokers: ["localhost:9092"],
});

const consumer = kafka.consumer({ groupId: "event-group" });

export const startConsumer = async () => {
    await consumer.connect();
    await consumer.subscribe({ topic: "events" });

    await consumer.run({
        eachBatch: async ({ batch }) => {
            const client = await pool.connect();

            try {
                await client.query("BEGIN");

                for (const message of batch.messages) {
                    if (!message.value) continue;

                    const event = JSON.parse(message.value.toString());

                    await client.query(
                        `
            INSERT INTO events 
            (event_type, user_id, session_id, product_id, timestamp, metadata)
            VALUES ($1, $2, $3, $4, $5, $6)
            `,
                        [
                            event.event_type,
                            event.user_id,
                            event.session_id,
                            event.product_id,
                            event.timestamp,
                            event.metadata,
                        ]
                    );
                }

                await client.query("COMMIT");
            } catch (err) {
                await client.query("ROLLBACK");
                console.error("Batch insert failed", err);
            } finally {
                client.release();
            }
        },
    });

    console.log("Consumer running");
};