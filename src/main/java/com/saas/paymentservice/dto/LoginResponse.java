package com.saas.paymentservice.dto;


public record LoginResponse(String token, String refreshToken) {
}
