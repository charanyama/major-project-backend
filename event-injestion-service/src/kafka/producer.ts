import { Kafka, Producer } from "kafkajs";

const kafka = new Kafka({
    clientId: "event-system",
    brokers: ["localhost:9092"],
});

let producer: Producer;

export const getProducer = async (): Promise<Producer> => {
    if (!producer) {
        producer = kafka.producer();
        await producer.connect();
        console.log("Kafka Producer Connected");
    }
    return producer;
};

export const publishEvent = async (event: any) => {
    const producer = await getProducer();

    await producer.send({
        topic: "events",
        messages: [
            {
                key: event.user_id || "anonymous",
                value: JSON.stringify(event),
            },
        ],
    });
};