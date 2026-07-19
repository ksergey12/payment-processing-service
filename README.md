# Payment Processing Service

A backend REST API for payment processing, built as a portfolio project demonstrating modern Java and Spring Boot practices relevant to fintech development.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security 6 + JWT (jjwt 0.12.x) |
| Persistence | PostgreSQL 16, Spring Data JPA, Flyway |
| Resilience | Resilience4j (Circuit Breaker, Retry) |
| Testing | JUnit 5, MockMvc, Testcontainers |
| Build | Maven |
| Runtime | Docker, Docker Compose |

## Key Features

- **JWT Authentication** — stateless auth with Spring Security 6 (`SecurityFilterChain`-based config, no `WebSecurityConfigurerAdapter`)
- **Idempotent Payments** — `Idempotency-Key` header prevents duplicate transactions on client retries
- **Resilience** — Circuit Breaker + Retry on external bank gateway calls via Resilience4j
- **Virtual Threads** — enabled via Spring Boot 3.2+ flag for improved I/O-bound throughput (Java 21 Project Loom)
- **Schema Versioning** — Flyway migrations, no Hibernate auto-DDL
- **Integration Tests** — real PostgreSQL via Testcontainers, no H2 mocks

## Project Structure

```
src/
├── main/java/com/saas/paymentservice/
│   ├── config/          # SecurityConfig
│   ├── controller/      # AuthController, TransactionController
│   ├── dto/             # Request/Response records
│   ├── entity/          # Transaction, User, IdempotencyKey
│   ├── exception/       # GlobalExceptionHandler, custom exceptions
│   ├── external/        # BankGatewayClient (mock)
│   ├── repository/      # JPA repositories
│   ├── security/        # JwtService, JwtAuthFilter, CustomUserDetailsService
│   └── service/         # TransactionService, UserService, BankGatewayService
├── main/resources/
│   ├── application.yml
│   └── db/migration/    # Flyway SQL migrations
└── test/java/           # Integration tests (Testcontainers)
```

## API Endpoints

### Auth

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | — | Register new user |
| POST | `/api/v1/auth/login` | — | Login, returns JWT |

### Transactions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/transactions` | Bearer JWT | Create transaction |
| GET | `/api/v1/transactions` | Bearer JWT | List all transactions |
| GET | `/api/v1/transactions/{id}` | Bearer JWT | Get transaction by ID |

#### Idempotency

Pass `Idempotency-Key: <uuid>` header on `POST /api/v1/transactions` to prevent duplicates on retry. Repeated requests with the same key return the original response without creating a new transaction.

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker

### Option 1 — Docker Compose (recommended)

```bash
git clone https://github.com/ksergey12/payment-processing-service.git
cd payment-processing-service
docker-compose up
```

Application starts on `http://localhost:8080`. PostgreSQL is started automatically.

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
{"token": "eyJhbGciOiJIUzI1NiJ9..."}
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

## Environment Variables

For production, replace hardcoded values in `application.yml` with environment variables:

| Variable | Description |
|---|---|
| `JWT_SECRET` | HMAC-SHA256 signing key (min 32 bytes) |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
