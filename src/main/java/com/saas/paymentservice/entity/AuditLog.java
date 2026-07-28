package com.saas.paymentservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(length = 500)
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    protected AuditLog() {
    }

    public AuditLog(UUID transactionId, UUID userId, String eventType, String details) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.eventType = eventType;
        this.details = details;
    }

    public UUID getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public UUID getUserId() { return userId; }
    public String getEventType() { return eventType; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
