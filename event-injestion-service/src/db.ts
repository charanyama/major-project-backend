import { Pool } from "pg";
import { config } from "./config/env";

export const pool = new Pool({
    connectionString: config.DATABASE_URL,
});

pool.on("error", (err) => {
    console.error("Unexpected DB error", err);
    process.exit(1);
});