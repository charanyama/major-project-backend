"use strict";
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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.userClient = exports.orderClient = exports.productClient = void 0;
const axios_1 = __importDefault(require("axios"));
const env_js_1 = __importDefault(require("../config/env.js"));
/**
 * Creates an Axios instance with logging interceptors.
 *
 * @param {string} baseURL  - Base URL of the downstream service
 * @param {string} label    - Label used in log lines, e.g. "ProductService"
 */
function createClient(baseURL, label) {
    const client = axios_1.default.create({
        baseURL,
        timeout: 10_000, // 10 seconds — match Spring Boot WebClient timeout
        headers: { "Content-Type": "application/json" },
    });
    client.interceptors.request.use((req) => {
        console.debug(`[${label}] → ${req.method?.toUpperCase()} ${req.baseURL}${req.url}`);
        return req;
    });
    client.interceptors.response.use((res) => {
        console.debug(`[${label}] ← HTTP ${res.status}`);
        return res;
    }, (err) => {
        const status = err.response?.status ?? "NO_RESPONSE";
        console.warn(`[${label}] ← ERROR HTTP ${status}: ${err.message}`);
        return Promise.reject(err);
    });
    return client;
}
exports.productClient = createClient(env_js_1.default.services.product, "ProductService");
exports.orderClient = createClient(env_js_1.default.services.order, "OrderService");
exports.userClient = createClient(env_js_1.default.services.user, "UserService");
//# sourceMappingURL=httpClient.js.map