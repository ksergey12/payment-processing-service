CREATE TABLE audit_log (
                           id UUID PRIMARY KEY,
                           transaction_id UUID NOT NULL,
                           user_id UUID NOT NULL,
                           event_type VARCHAR(50) NOT NULL,
                           details VARCHAR(500),
                           created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_log_transaction_id ON audit_log(transaction_id);
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);