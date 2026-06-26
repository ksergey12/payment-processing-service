package com.saas.paymentservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoadTestController {

    @GetMapping("/api/v1/load-test/slow")
    public String slowEndpoint() throws InterruptedException {
        // Имитация блокирующего I/O — например, медленный внешний вызов или JDBC-запрос
        Thread.sleep(2000);
        return "Thread: " + Thread.currentThread();
    }
}