CREATE TABLE idempotency_keys (
                                  idempotency_key VARCHAR(255) PRIMARY KEY,
                                  transaction_id UUID NOT NULL REFERENCES transactions(id),
                                  created_at TIMESTAMP NOT NULL
);