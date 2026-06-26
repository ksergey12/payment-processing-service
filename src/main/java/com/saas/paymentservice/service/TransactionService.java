package com.saas.paymentservice.service;

import com.saas.paymentservice.dto.CreateTransactionRequest;
import com.saas.paymentservice.dto.TransactionResponse;
import com.saas.paymentservice.entity.IdempotencyKey;
import com.saas.paymentservice.entity.Transaction;
import com.saas.paymentservice.entity.TransactionStatus;
import com.saas.paymentservice.repository.IdempotencyKeyRepository;
import com.saas.paymentservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public TransactionResponse createTransaction(CreateTransactionRequest request, String idempotencyKey) {
        if (idempotencyKey != null) {
            var existingKey = idempotencyKeyRepository.findById(idempotencyKey);
            if (existingKey.isPresent()) {
                UUID existingTransactionId = existingKey.get().getTransactionId();
                return getTransaction(existingTransactionId);
            }
        }

        Transaction transaction = new Transaction(request.amount(), request.currency());
        Transaction saved = transactionRepository.save(transaction);

        String confirmation = bankGatewayService.confirmPayment(saved.getId());
        if (confirmation.startsWith("CONFIRMED")) {
            saved.setStatus(TransactionStatus.COMPLETED);
        } else {
            saved.setStatus(TransactionStatus.PENDING);
        }
        saved = transactionRepository.save(saved);

        if (idempotencyKey != null) {
            idempotencyKeyRepository.save(new IdempotencyKey(idempotencyKey, saved.getId()));
        }

        return TransactionResponse.from(saved);
    }

    public TransactionResponse getTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
        return TransactionResponse.from(transaction);
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
