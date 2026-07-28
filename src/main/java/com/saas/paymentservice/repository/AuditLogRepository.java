package com.saas.paymentservice.repository;

import com.saas.paymentservice.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findAllByTransactionIdOrderByCreatedAtAsc(UUID transactionId);
}
