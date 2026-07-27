ALTER TABLE transactions
    ADD COLUMN user_id UUID REFERENCES users(id);
