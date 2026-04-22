import express from "express";
import { config } from "./config/env";
import eventRoutes from "./routes/events";

const app = express();

app.use(express.json());

app.use("/events", eventRoutes);

app.get("/health", (_, res) => {
    res.json({ status: `Injestion Service is running on the port: ${config.PORT}` });
});

app.listen(config.PORT, () => {
    console.log(`Event Collector running on PORT ${config.PORT}`);
});