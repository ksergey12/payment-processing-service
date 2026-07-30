package com.saas.paymentservice.service;

import com.saas.paymentservice.dto.CreateTransactionRequest;
import com.saas.paymentservice.dto.TransactionResponse;
import com.saas.paymentservice.entity.AuditEventType;
import com.saas.paymentservice.entity.IdempotencyKey;
import com.saas.paymentservice.entity.Transaction;
import com.saas.paymentservice.entity.TransactionStatus;
import com.saas.paymentservice.repository.IdempotencyKeyRepository;
import com.saas.paymentservice.repository.TransactionRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.validation.annotation.Validated;


@Service
@Validated
public class TransactionService {

    private final Counter transactionCreatedCounter;
    private final Counter transactionCompletedCounter;
    private final Counter transactionFailedCounter;
    private final Timer transactionTimer;

    private final TransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final BankGatewayService bankGatewayService;
    private final AuditService auditService;

    public TransactionService(TransactionRepository transactionRepository,
                              IdempotencyKeyRepository idempotencyKeyRepository,
                              BankGatewayService bankGatewayService,
                              AuditService auditService,
                              MeterRegistry meterRegistry) {
        this.transactionRepository = transactionRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.bankGatewayService = bankGatewayService;
        this.auditService = auditService;

        this.transactionCreatedCounter = Counter.builder("transactions.created")
                .description("Total transactions created")
                .register(meterRegistry);
        this.transactionCompletedCounter = Counter.builder("transactions.completed")
                .description("Total transactions completed")
                .register(meterRegistry);
        this.transactionFailedCounter = Counter.builder("transactions.failed")
                .description("Total transactions failed or pending")
                .register(meterRegistry);
        this.transactionTimer = Timer.builder("transactions.processing.time")
                .description("Time to process a transaction")
                .register(meterRegistry);
    }

    @Transactional
    public TransactionResponse createTransaction(@Valid CreateTransactionRequest request,
                                                 String idempotencyKey,
                                                 UUID userId) {
        return transactionTimer.record(() -> {

            if (idempotencyKey != null) {
                var existingKey = idempotencyKeyRepository.findById(idempotencyKey);
                if (existingKey.isPresent()) {
                    UUID existingId = existingKey.get().getTransactionId();
                    auditService.record(existingId, userId,
                            AuditEventType.IDEMPOTENT_REQUEST,
                            "Duplicate request with key: " + idempotencyKey);
                    return getTransaction(existingId, userId, false);
                }
            }

            Transaction transaction = new Transaction(request.amount(), request.currency(), userId);
            Transaction saved = transactionRepository.save(transaction);

            auditService.record(saved.getId(), userId,
                    AuditEventType.TRANSACTION_CREATED,
                    "amount=" + request.amount() + " currency=" + request.currency());

            if (idempotencyKey != null) {
                idempotencyKeyRepository.save(new IdempotencyKey(idempotencyKey, saved.getId()));
            }

            String confirmation = bankGatewayService.confirmPayment(saved.getId());
            if (confirmation.startsWith("CONFIRMED")) {
                saved.setStatus(TransactionStatus.COMPLETED);
                transactionCompletedCounter.increment();
                auditService.record(saved.getId(), userId,
                        AuditEventType.TRANSACTION_COMPLETED,
                        "confirmation=" + confirmation);
            } else {
                saved.setStatus(TransactionStatus.PENDING);
                transactionFailedCounter.increment();
                auditService.record(saved.getId(), userId,
                        AuditEventType.TRANSACTION_FAILED,
                        "Bank gateway returned: " + confirmation);
            }
            saved = transactionRepository.save(saved);
            transactionCreatedCounter.increment();

            return TransactionResponse.from(saved);
        });
    }

    public TransactionResponse getTransaction(UUID id, UUID userId, boolean isAdmin) {
        Transaction transaction;
        if (isAdmin) {
            transaction = transactionRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
        } else {
            transaction = transactionRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
        }
        return TransactionResponse.from(transaction);
    }

    public Page<TransactionResponse> getAllTransactions(UUID userId,
                                                        boolean isAdmin,
                                                        Pageable pageable) {
        if (isAdmin) {
            return transactionRepository.findAll(pageable)
                    .map(TransactionResponse::from);
        }
        return transactionRepository.findAllByUserId(userId, pageable)
                .map(TransactionResponse::from);
    }
}
