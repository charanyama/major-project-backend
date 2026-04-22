/**
 * config/env.ts
 *
 * Single source of truth for all runtime configuration.
 * Reads from process.env (populated by dotenv in app.ts).
 *
 * Fails fast at startup if required variables are missing.
 */

function requireEnv(name: string): string {
    const value = process.env[name];
    if (!value) {
        throw new Error(`Missing required environment variable: ${name}`);
    }
    return value;
}

function optionalEnv(name: string, fallback: string): string {
    return process.env[name] ?? fallback;
}

interface Config {
    port: number;
    nodeEnv: string;

    jwt: {
        secret: string;
        userIdClaim: string;
    };

    services: {
        user: string;
        product: string;
        order: string;
    };

    corsOrigins: string[];
    logFormat: string;
}

const config: Config = {
    port: parseInt(optionalEnv("PORT", "3000"), 10),
    nodeEnv: optionalEnv("NODE_ENV", "development"),
    jwt: {
        secret: requireEnv("JWT_SECRET"),
        userIdClaim: optionalEnv("JWT_USER_ID_CLAIM", "sub"),
    },
    services: {
        user: optionalEnv("USER_SERVICE_URL", "http://localhost:8081"),
        product: optionalEnv("PRODUCT_SERVICE_URL", "http://localhost:8082"),
        order: optionalEnv("ORDER_SERVICE_URL", "http://localhost:8083"),
    },
    corsOrigins: optionalEnv("CORS_ORIGINS", "http://localhost:3000")
        .split(",")
        .map((o) => o.trim()),

    logFormat: optionalEnv("LOG_FORMAT", "dev"),
};

export default config;