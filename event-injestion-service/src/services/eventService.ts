import { DBEvent } from "../types/event";
import { publishEvent } from "../kafka/producer";

export const ingestEvent = async (event: DBEvent) => {
    // Only responsibility: forward to Kafka
    await publishEvent(event);
};