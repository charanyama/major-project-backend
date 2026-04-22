package com.virtualstore.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * InvalidatedToken
 *
 * Tracks blocklisted JWTs so signed-out tokens stay invalid even though JWTs
 * are otherwise stateless.
 *
 * In MongoDB the TTL index auto-purged these entries; with Postgres the
 * cleanup must be handled separately (e.g. scheduled job) so the table
 * doesn't grow indefinitely.
 */
@Entity
@Table(name = "invalidated_tokens", indexes = {
        @Index(name = "idx_invalidated_tokens_jti", columnList = "jti", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    /** The JTI (JWT ID) claim from the invalidated token */
    @Column(name = "jti", nullable = false, unique = true)
    private String jti;

    /** The userId who signed out */
    @Column(name = "user_id")
    private String userId;

    /**
     * When this record should be eligible for cleanup.
     * No automatic TTL exists; implement a scheduled purge if needed.
     */
    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "invalidated_at")
    private Instant invalidatedAt;

    @PrePersist
    private void ensureId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (invalidatedAt == null) {
            invalidatedAt = Instant.now();
        }
    }
}
