package com.saas.paymentservice;

import com.saas.paymentservice.dto.LoginRequest;
import com.saas.paymentservice.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void register_shouldReturn201_whenValidRequest() {
        var request = new RegisterRequest("testuser", "password123");

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void register_shouldReturn409_whenUsernameAlreadyExists() {
        var request = new RegisterRequest("duplicateuser", "password123");
        restTemplate.postForEntity("/api/v1/auth/register", request, Void.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void login_shouldReturnToken_whenValidCredentials() {
        var registerRequest = new RegisterRequest("loginuser", "password123");
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Void.class);

        var loginRequest = new LoginRequest("loginuser", "password123");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("token");
    }

    @Test
    void login_shouldReturn401_whenWrongPassword() {
        var registerRequest = new RegisterRequest("wrongpassuser", "password123");
        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, Void.class);

        var loginRequest = new LoginRequest("wrongpassuser", "wrongpassword");
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
