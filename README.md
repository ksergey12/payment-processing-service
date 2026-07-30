# Payment Processing Service

![CI](https://github.com/ksergey12/payment-processing-service/actions/workflows/ci.yml/badge.svg)

A backend REST API for payment processing, built as a portfolio project demonstrating modern Java and Spring Boot practices relevant to fintech development.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security 6 + JWT (jjwt 0.12.x) |
| Persistence | PostgreSQL 16, Spring Data JPA, Flyway |
| Resilience | Resilience4j (Circuit Breaker, Retry) |
| Observability | Micrometer, Loki, Grafana |
| API Docs | SpringDoc OpenAPI 2.x (Swagger UI) |
| Testing | JUnit 5, MockMvc, Testcontainers |
| Build | Maven |
| Runtime | Docker, Docker Compose |

## Key Features

- **JWT Authentication** — stateless auth with Spring Security 6 (`SecurityFilterChain`-based config, no `WebSecurityConfigurerAdapter`)
- **Role-based Authorization** — `USER` creates and views own transactions; `ADMIN` views all transactions via `@PreAuthorize`
- **Data Isolation** — users can only access their own transactions; cross-user access returns `404` to prevent information leakage
- **Idempotent Payments** — `Idempotency-Key` header prevents duplicate transactions on client retries
- **Resilience** — Circuit Breaker + Retry on external bank gateway calls via Resilience4j
- **Virtual Threads** — enabled via Spring Boot 3.2+ flag for improved I/O-bound throughput (Java 21 Project Loom)
- **Observability** — structured JSON logs via Logback + MDC tracing (`traceId`, `userId`), Micrometer metrics, Loki + Grafana for log aggregation
- **Schema Versioning** — Flyway migrations (V1–V8), no Hibernate auto-DDL
- **Integration Tests** — real PostgreSQL via Testcontainers, no H2 mocks
- **API Documentation** — Swagger UI via SpringDoc OpenAPI 2.x

## Project Structure

```
payment-processing-service/
├── docker/
│   ├── loki-config.yml
│   └── grafana/provisioning/datasources/loki.yml
├── src/
│   ├── main/java/com/konstantin/paymentservice/
│   │   ├── config/          # SecurityConfig, OpenApiConfig, WebConfig
│   │   ├── controller/      # AuthController, TransactionController
│   │   ├── dto/             # Request/Response records
│   │   ├── entity/          # Transaction, User, IdempotencyKey, AuditLog
│   │   ├── exception/       # GlobalExceptionHandler, custom exceptions
│   │   ├── external/        # BankGatewayClient (mock)
│   │   ├── filter/          # MdcFilter
│   │   ├── repository/      # JPA repositories
│   │   ├── security/        # JwtService, JwtAuthFilter, CustomUserDetailsService
│   │   └── service/         # TransactionService, UserService, BankGatewayService, AuditService
│   ├── main/resources/
│   │   ├── application.yml
│   │   ├── logback-spring.xml
│   │   └── db/migration/    # Flyway SQL migrations V1–V9
│   └── test/java/           # Integration tests (Testcontainers + MockMvc)
└── docker-compose.yml
```

## API Endpoints

### Auth

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | — | Register new user (role: USER) |
| POST | `/api/v1/auth/login` | — | Login, returns access + refresh token |
| POST | `/api/v1/auth/refresh` | — | Get new access token via refresh token |
| POST | `/api/v1/auth/logout` | Bearer JWT | Revoke all refresh tokens |
| POST | `/api/v1/auth/register/admin` | ADMIN | Register new admin user |

### Transactions

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/transactions` | USER | Create transaction |
| GET | `/api/v1/transactions` | USER, ADMIN | List transactions (USER: own only, ADMIN: all) |
| GET | `/api/v1/transactions/{id}` | USER, ADMIN | Get transaction by ID |

### API Documentation

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3.0 JSON spec |

#### Pagination & Sorting

`GET /api/v1/transactions` supports pagination and sorting via query parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page number (zero-based) |
| `size` | `20` | Page size |
| `sort` | `createdAt,desc` | Field and direction |

```bash
# Page 0, 10 results, sorted by amount descending
curl "http://localhost:8080/api/v1/transactions?page=0&size=10&sort=amount,desc" \
  -H "Authorization: Bearer <token>"
```

Response includes pagination metadata:
```json
{
  "content": [...],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

#### Idempotency

Pass `Idempotency-Key: <uuid>` header on `POST /api/v1/transactions` to prevent duplicates on retry. Repeated requests with the same key return the original response without creating a new transaction.

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker

### Option 1 — Docker Compose (recommended)

```bash
git clone https://github.com/<your-username>/payment-processing-service.git
cd payment-processing-service
cp .env.example .env   # fill in your values
docker-compose up
```

| Service | URL |
|---------|-----|
| Application | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Grafana (logs) | http://localhost:3000 |
| Loki | http://localhost:3100 |

### Option 2 — IDE + local Docker

1. Start PostgreSQL container:

```bash
docker run --name payment-postgres \
  -e POSTGRES_DB=payment_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v payment-postgres-data:/var/lib/postgresql/data \
  --restart unless-stopped \
  -d postgres:16
```

2. Run the application from your IDE (`PaymentServiceApplication.java`) or via Maven:

```bash
mvn spring-boot:run
```

Flyway will apply migrations automatically on startup.

## Usage Examples

### Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "securePass123"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "securePass123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Refresh Access Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<refresh-token>"}'
```

### Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <access-token>"
```

### Create Transaction (with idempotency)

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -H "Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{"amount": 150.00, "currency": "EUR"}'
```

Response:
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "amount": 150.00,
  "currency": "EUR",
  "status": "COMPLETED",
  "createdAt": "2026-07-19T14:00:00Z"
}
```

## Running Tests

Integration tests use Testcontainers — Docker must be running.

```bash
mvn test
```

Tests spin up a dedicated PostgreSQL container automatically. No manual setup required.

## Design Decisions

**UUID primary keys** — avoids leaking record counts to clients; works naturally in distributed systems without a centralized sequence.

**Idempotency as a separate table** — decouples infrastructure concern (API safety) from business entity (`Transaction`), and allows reuse across multiple endpoints.

**`ddl-auto: validate`** — Hibernate validates schema against Flyway-managed migrations rather than generating DDL. Matches fintech production practices where schema changes go through a review process.

**Virtual Threads enabled** — benchmarked on 50 concurrent requests with a 2s blocking I/O call: platform threads (pool=10) took ~10s; virtual threads took ~2s. Enabled via `spring.threads.virtual.enabled: true` with no code changes required.

**`jakarta.*` namespace** — project targets Spring Boot 3.x / Jakarta EE 9+; all imports use `jakarta.*` (not `javax.*`).

**404 on cross-user access, not 403** — when a USER requests another user's transaction, the API returns `404 Not Found` rather than `403 Forbidden`. This avoids leaking information about whether a resource exists — a standard practice in fintech APIs.

**ADMIN and USER as separate roles** — ADMIN does not inherit USER permissions. An admin can view all transactions for audit purposes but cannot create transactions. This separation of concerns prevents accidental privilege escalation.

**Refresh token rotation** — every `/auth/refresh` call issues a new refresh token and implicitly invalidates the old one. If a stolen token is used first by an attacker, the legitimate user gets `401` on their next refresh attempt — a standard defence against token theft. Refresh tokens are stored in the database, enabling server-side revocation on logout.

**Structured logs with MDC** — every request is tagged with a `traceId` (UUID) and `userId` in the MDC context. This allows filtering all log entries for a single request or user in Grafana/Loki without any code changes in individual services.

## Environment Variables

For production, replace hardcoded values in `application.yml` with environment variables:

| Variable | Description |
|---|---|
| `JWT_SECRET` | HMAC-SHA256 signing key (min 32 bytes) |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
