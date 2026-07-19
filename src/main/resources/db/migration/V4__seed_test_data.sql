-- Тестовые данные для локальной разработки.
-- Пароли захешированы через BCrypt (совместимо со Spring Security BCryptPasswordEncoder):
--   admin  / admin123
--   user1  / password123

INSERT INTO users (id, username, password_hash, roles, enabled, created_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin', '$2b$10$yHQR3CxIqik3vUCBdQwm8.mq5GxsroMN7X7oF.RFG5xWuOv3.vt9S', 'ROLE_ADMIN', true, now()),
    ('22222222-2222-2222-2222-222222222222', 'user1', '$2b$10$RyvOjjWDPylnPcdCkvmsuuaMX78ZCeIUYToAE/h//TjSsoQ28m532', 'ROLE_USER', true, now());

INSERT INTO transactions (id, amount, currency, status, created_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 150.0000, 'EUR', 'COMPLETED', now() - interval '2 days'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 42.5000,  'EUR', 'PENDING',   now() - interval '1 day'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 999.9900, 'USD', 'FAILED',    now());

INSERT INTO idempotency_keys (idempotency_key, transaction_id, created_at) VALUES
    ('idem-key-001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', now() - interval '2 days'),
    ('idem-key-002', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', now() - interval '1 day');
