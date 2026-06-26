package com.saas.paymentservice.service;

import com.saas.paymentservice.external.BankGatewayClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BankGatewayService {

    private static final Logger log = LoggerFactory.getLogger(BankGatewayService.class);

    private final BankGatewayClient bankGatewayClient;

    public BankGatewayService(BankGatewayClient bankGatewayClient) {
        this.bankGatewayClient = bankGatewayClient;
    }

    @Retry(name = "bankGateway")
    @CircuitBreaker(name = "bankGateway", fallbackMethod = "fallbackConfirmation")
    public String confirmPayment(UUID transactionId) {
        return bankGatewayClient.confirmPayment(transactionId);
    }

    private String fallbackConfirmation(UUID transactionId, Throwable throwable) {
        log.warn("Bank gateway unavailable for transaction {}, reason: {}",
                transactionId, throwable.getMessage());
        return "PENDING-MANUAL-REVIEW-" + transactionId;
    }
}
