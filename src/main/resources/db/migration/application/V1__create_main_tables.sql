--TRANSFER
CREATE SEQUENCE IF NOT EXISTS transfer_id_sequence;
CREATE TABLE IF NOT EXISTS transfer (
    id INTEGER NOT NULL DEFAULT nextval('transfer_id_sequence'),
    external_id VARCHAR NOT NULL,
    payer_external_id VARCHAR NOT NULL,
    payee_external_id VARCHAR NOT NULL,
    transfer_value VARCHAR NOT NULL,
    transfer_type VARCHAR NOT NULL check (transfer_type in ('NATURAL_TO_NATURAL', 'NATURAL_TO_LEGAL')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);
ALTER SEQUENCE transfer_id_sequence OWNED BY transfer.id;
CREATE INDEX IF NOT EXISTS idx_transfer_payer_external_id ON transfer(payer_external_id);
CREATE INDEX IF NOT EXISTS idx_transfer_payee_external_id ON transfer(payee_external_id);
CREATE INDEX IF NOT EXISTS idx_transfer_created_at ON transfer(created_at);

--Wallet
CREATE SEQUENCE IF NOT EXISTS wallet_id_sequence;
CREATE TABLE IF NOT EXISTS "wallet"(
    id INTEGER NOT NULL DEFAULT nextval('wallet_id_sequence'),
    external_id VARCHAR NOT NULL,
    owner_name VARCHAR NOT NULL,
    document VARCHAR NOT NULL,
    balance VARCHAR NOT NULL,
    email VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_wallet_document UNIQUE (document),
    CONSTRAINT uk_wallet_external_id UNIQUE (external_id),
    CONSTRAINT uk_wallet_email UNIQUE(email)
);
ALTER SEQUENCE wallet_id_sequence OWNED BY "wallet".id;
CREATE INDEX IF NOT EXISTS idx_wallet_external_id ON "wallet"(external_id);
CREATE INDEX IF NOT EXISTS idx_wallet_owner_name ON "wallet"(owner_name);
CREATE INDEX IF NOT EXISTS idx_wallet_email ON "wallet"(email);
CREATE INDEX IF NOT EXISTS idx_wallet_document ON "wallet"(document);