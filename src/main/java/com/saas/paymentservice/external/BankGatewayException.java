package com.saas.paymentservice.external;

public class BankGatewayException extends RuntimeException {
    public BankGatewayException(String message) {
        super(message);
    }
}
