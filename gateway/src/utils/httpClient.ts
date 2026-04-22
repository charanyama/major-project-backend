/**
 * utils/httpClient.js
 *
 * Pre-configured Axios instances for each downstream microservice.
 *
 * Each client:
 *   - Has a fixed baseURL from config
 *   - Enforces a 10-second timeout
 *   - Logs outgoing requests and incoming responses (debug)
 *
 * Used by the BFF orchestration layer (dashboardController).
 * NOT used for proxied routes — those go through express-http-proxy directly.
 */

import axios from "axios";
import config from "../config/env.js";

/**
 * Creates an Axios instance with logging interceptors.
 *
 * @param {string} baseURL  - Base URL of the downstream service
 * @param {string} label    - Label used in log lines, e.g. "ProductService"
 */
function createClient(baseURL: string, label: string) {
    const client = axios.create({
        baseURL,
        timeout: 10_000, // 10 seconds — match Spring Boot WebClient timeout
        headers: { "Content-Type": "application/json" },
    });

    client.interceptors.request.use((req) => {
        console.debug(`[${label}] → ${req.method?.toUpperCase()} ${req.baseURL}${req.url}`);
        return req;
    });

    client.interceptors.response.use(
        (res) => {
            console.debug(`[${label}] ← HTTP ${res.status}`);
            return res;
        },
        (err) => {
            const status = err.response?.status ?? "NO_RESPONSE";
            console.warn(`[${label}] ← ERROR HTTP ${status}: ${err.message}`);
            return Promise.reject(err);
        }
    );

    return client;
}

export const productClient = createClient(config.services.product, "ProductService");
export const orderClient = createClient(config.services.order, "OrderService");
export const userClient = createClient(config.services.user, "UserService");