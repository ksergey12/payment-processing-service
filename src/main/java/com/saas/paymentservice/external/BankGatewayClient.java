package com.saas.paymentservice.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class BankGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(BankGatewayClient.class);

    public String confirmPayment(UUID transactionId) {
        log.info("Calling external bank gateway for transaction {}", transactionId);

        // Имитация нестабильности: ~40% запросов "падают"
        if (ThreadLocalRandom.current().nextInt(100) < 40) {
            throw new BankGatewayException("Bank gateway timeout for transaction " + transactionId);
        }

        return "CONFIRMED-" + transactionId;
    }
}
