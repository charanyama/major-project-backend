import dotenv from "dotenv";

dotenv.config();

export const config = {
    PORT: parseInt(process.env.PORT || "3000"),
    DATABASE_URL: process.env.DATABASE_URL as string,
    BATCH_SIZE: parseInt(process.env.BATCH_SIZE || "50"),
    FLUSH_INTERVAL: parseInt(process.env.FLUSH_INTERVAL_MS || "1000"),
};