package com.virtualstore.user_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * InvalidatedToken
 *
 * MongoDB document representing a JWT that has been explicitly invalidated
 * (i.e. the user signed out before the token expired naturally).
 *
 * How it works:
 * 1. User calls POST /auth/signout with their access token.
 * 2. We extract the token's JTI (JWT ID claim) and expiry.
 * 3. We store them here.
 * 4. JwtAuthenticationFilter checks this collection before
 * allowing any request through.
 *
 * TTL Index:
 * MongoDB automatically deletes documents when `expiresAt` passes.
 * This means the blocklist self-cleans — no manual purge needed.
 * 
 * @Indexed(expireAfterSeconds = 0) tells MongoDB to use the document's
 *                             own `expiresAt` field as the TTL boundary.
 */
@Document(collection = "invalidated_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {

    @Id
    private String id;

    /** The JTI (JWT ID) claim from the invalidated token */
    @Indexed(unique = true)
    private String jti;

    /** The userId who signed out */
    private String userId;

    /**
     * When this document should be auto-deleted by MongoDB.
     * Set to the token's own expiry time — once the token would
     * have expired anyway, the blocklist entry is irrelevant.
     */
    @Indexed(expireAfter = "0s")
    private Instant expiresAt;

    private Instant invalidatedAt;
}