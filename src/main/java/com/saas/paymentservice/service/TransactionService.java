package com.saas.paymentservice.service;

import com.saas.paymentservice.dto.CreateTransactionRequest;
import com.saas.paymentservice.dto.TransactionResponse;
import com.saas.paymentservice.entity.Transaction;
import com.saas.paymentservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        Transaction transaction = new Transaction(request.amount(), request.currency());
        Transaction saved = transactionRepository.save(transaction);
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
