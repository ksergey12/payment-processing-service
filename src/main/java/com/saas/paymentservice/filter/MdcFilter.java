package com.saas.paymentservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Уникальный ID каждого запроса для трассировки
            String traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
            response.setHeader("X-Trace-Id", traceId);

            // Username текущего пользователя если аутентифицирован
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() &&
                    !"anonymousUser".equals(auth.getPrincipal())) {
                MDC.put("userId", auth.getName());
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear(); // Обязательно очищаем MDC после запроса
        }
    }
}
