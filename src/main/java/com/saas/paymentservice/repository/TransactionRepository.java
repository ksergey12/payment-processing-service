package com.saas.paymentservice.repository;

import com.saas.paymentservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findAllByUserId(UUID userId, Pageable pageable);
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
}
