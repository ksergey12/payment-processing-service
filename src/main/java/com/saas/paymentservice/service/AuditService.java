package com.saas.paymentservice.service;

import com.saas.paymentservice.entity.AuditLog;
import com.saas.paymentservice.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(UUID transactionId, UUID userId, String eventType, String details) {
        auditLogRepository.save(new AuditLog(transactionId, userId, eventType, details));
        log.info("AUDIT event={} transactionId={} userId={} details={}",
                eventType, transactionId, userId, details);
    }
}
