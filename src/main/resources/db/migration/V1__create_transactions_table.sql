CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              amount NUMERIC(19, 4) NOT NULL,
                              currency VARCHAR(3) NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              created_at TIMESTAMP NOT NULL
);