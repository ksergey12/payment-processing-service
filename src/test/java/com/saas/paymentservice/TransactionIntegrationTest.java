package com.saas.paymentservice;

import com.saas.paymentservice.dto.CreateTransactionRequest;
import com.saas.paymentservice.dto.LoginRequest;
import com.saas.paymentservice.dto.RegisterRequest;
import com.saas.paymentservice.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    private HttpHeaders authHeaders;

    @BeforeEach
    void setUp() {
        // Регистрируем и логиним тестового юзера перед каждым тестом
        String username = "txuser_" + System.currentTimeMillis();
        restTemplate.postForEntity("/api/v1/auth/register",
                new RegisterRequest(username, "password123"), Void.class);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login",
                new LoginRequest(username, "password123"), String.class);

        String body = loginResponse.getBody();
        // Простое извлечение токена из JSON без Jackson — для краткости
        String token = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void createTransaction_shouldReturn201_whenValidRequest() {
        var request = new CreateTransactionRequest(new BigDecimal("100.00"), "EUR");
        var entity = new HttpEntity<>(request, authHeaders);

        ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(
                "/api/v1/transactions", entity, TransactionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().amount()).isEqualByComparingTo("100.00");
        assertThat(response.getBody().currency()).isEqualTo("EUR");
        assertThat(response.getBody().id()).isNotNull();
    }

    @Test
    void createTransaction_shouldReturn401_whenNoToken() {
        var request = new CreateTransactionRequest(new BigDecimal("100.00"), "EUR");
        var entity = new HttpEntity<>(request);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/transactions", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void idempotency_shouldReturnSameTransaction_whenSameKeyUsed() {
        var request = new CreateTransactionRequest(new BigDecimal("50.00"), "USD");
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(authHeaders);
        headers.set("Idempotency-Key", "test-idempotency-key-123");

        var entity = new HttpEntity<>(request, headers);

        ResponseEntity<TransactionResponse> first = restTemplate.postForEntity(
                "/api/v1/transactions", entity, TransactionResponse.class);
        ResponseEntity<TransactionResponse> second = restTemplate.postForEntity(
                "/api/v1/transactions", entity, TransactionResponse.class);

        assertThat(first.getBody().id()).isEqualTo(second.getBody().id());
    }
}
