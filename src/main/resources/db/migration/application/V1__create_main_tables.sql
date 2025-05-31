--TRANSFER
CREATE TABLE IF NOT EXISTS transfer (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    external_id VARCHAR NOT NULL,
    payer_external_id VARCHAR NOT NULL,
    payee_external_id VARCHAR NOT NULL,
    transfer_value VARCHAR NOT NULL,
    transfer_type VARCHAR NOT NULL check (transfer_type in ('NATURAL_TO_NATURAL', 'NATURAL_TO_LEGAL')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_transfer_payer_external_id ON transfer(payer_external_id);
CREATE INDEX IF NOT EXISTS idx_transfer_payee_external_id ON transfer(payee_external_id);
CREATE INDEX IF NOT EXISTS idx_transfer_created_at ON transfer(created_at);

--Wallet
CREATE TABLE IF NOT EXISTS wallet(
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    external_id VARCHAR NOT NULL,
    owner_name VARCHAR NOT NULL,
    document VARCHAR NOT NULL,
    balance NUMERIC NOT NULL,
    email VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    CONSTRAINT uk_wallet_document UNIQUE (document),
    CONSTRAINT uk_wallet_external_id UNIQUE (external_id),
    CONSTRAINT uk_wallet_email UNIQUE(email)
);
CREATE INDEX IF NOT EXISTS idx_wallet_external_id ON wallet(external_id);
CREATE INDEX IF NOT EXISTS idx_wallet_owner_name ON wallet(owner_name);
CREATE INDEX IF NOT EXISTS idx_wallet_email ON wallet(email);
CREATE INDEX IF NOT EXISTS idx_wallet_document ON wallet(document);