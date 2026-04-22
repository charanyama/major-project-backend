export interface EventPayload {
    eventType: string;
    userId?: string;
    productId?: string;
    sessionId?: string;
    metadata?: Record<string, any>;
}

export interface DBEvent {
    event_type: string;
    user_id: string | null;
    product_id: string | null;
    session_id: string;
    timestamp: Date;
    metadata: Record<string, any>;
}