package com.saas.paymentservice.entity;

public final class AuditEventType {
    public static final String TRANSACTION_CREATED = "TRANSACTION_CREATED";
    public static final String TRANSACTION_COMPLETED = "TRANSACTION_COMPLETED";
    public static final String TRANSACTION_FAILED = "TRANSACTION_FAILED";
    public static final String IDEMPOTENT_REQUEST = "IDEMPOTENT_REQUEST";

    private AuditEventType() {}
}
