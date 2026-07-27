package com.saas.paymentservice.service;

import com.saas.paymentservice.dto.CreateTransactionRequest;
import com.saas.paymentservice.dto.TransactionResponse;
import com.saas.paymentservice.entity.IdempotencyKey;
import com.saas.paymentservice.entity.Transaction;
import com.saas.paymentservice.entity.TransactionStatus;
import com.saas.paymentservice.repository.IdempotencyKeyRepository;
import com.saas.paymentservice.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final BankGatewayService bankGatewayService;

    public TransactionService(TransactionRepository transactionRepository,
                              IdempotencyKeyRepository idempotencyKeyRepository,
                              BankGatewayService bankGatewayService) {
        this.transactionRepository = transactionRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.bankGatewayService = bankGatewayService;
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request,
                                                 String idempotencyKey,
                                                 UUID userId) {
        if (idempotencyKey != null) {
            var existingKey = idempotencyKeyRepository.findById(idempotencyKey);
            if (existingKey.isPresent()) {
                return getTransaction(existingKey.get().getTransactionId(), userId);
            }
        }

        Transaction transaction = new Transaction(request.amount(), request.currency(), userId);
        Transaction saved = transactionRepository.save(transaction);

        if (idempotencyKey != null) {
            idempotencyKeyRepository.save(new IdempotencyKey(idempotencyKey, saved.getId()));
        }

        String confirmation = bankGatewayService.confirmPayment(saved.getId());
        if (confirmation.startsWith("CONFIRMED")) {
            saved.setStatus(TransactionStatus.COMPLETED);
        } else {
            saved.setStatus(TransactionStatus.PENDING);
        }
        saved = transactionRepository.save(saved);

        return TransactionResponse.from(saved);
    }

    public TransactionResponse getTransaction(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
        return TransactionResponse.from(transaction);
    }

    public Page<TransactionResponse> getAllTransactions(UUID userId, Pageable pageable) {
        return transactionRepository.findAllByUserId(userId, pageable)
                .map(TransactionResponse::from);
    }
}
