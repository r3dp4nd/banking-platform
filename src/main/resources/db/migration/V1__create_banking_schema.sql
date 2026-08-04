CREATE TABLE bank_account
(
    id             UUID PRIMARY KEY,
    account_number VARCHAR(32)              NOT NULL,
    currency       VARCHAR(3)               NOT NULL,
    balance        NUMERIC(19, 2)           NOT NULL,
    status         VARCHAR(20)              NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_bank_account_number
        UNIQUE (account_number),

    CONSTRAINT ck_bank_account_balance_non_negative
        CHECK (balance >= 0),

    CONSTRAINT ck_bank_account_currency
        CHECK (currency IN ('PEN', 'USD')),

    CONSTRAINT ck_bank_account_status
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'))
);

CREATE TABLE bank_transfer
(
    id                     UUID PRIMARY KEY,
    source_account_id      UUID                     NOT NULL,
    destination_account_id UUID                     NOT NULL,
    amount                 NUMERIC(19, 2)           NOT NULL,
    currency               VARCHAR(3)               NOT NULL,
    description            VARCHAR(140)             NOT NULL,
    idempotency_key        VARCHAR(128)             NOT NULL,
    status                 VARCHAR(20)              NOT NULL,
    failure_reason         VARCHAR(100),
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at           TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_bank_transfer_source_account
        FOREIGN KEY (source_account_id)
            REFERENCES bank_account (id),

    CONSTRAINT fk_bank_transfer_destination_account
        FOREIGN KEY (destination_account_id)
            REFERENCES bank_account (id),

    CONSTRAINT uk_bank_transfer_idempotency_key
        UNIQUE (idempotency_key),

    CONSTRAINT ck_bank_transfer_accounts_differ
        CHECK (source_account_id <> destination_account_id),

    CONSTRAINT ck_bank_transfer_amount_positive
        CHECK (amount > 0),

    CONSTRAINT ck_bank_transfer_currency
        CHECK (currency IN ('PEN', 'USD')),

    CONSTRAINT ck_bank_transfer_status
        CHECK (
            status IN (
                       'PENDING',
                       'COMPLETED',
                       'REJECTED',
                       'FAILED'
                )
            ),

    CONSTRAINT ck_bank_transfer_completion
        CHECK (
            (
                status = 'PENDING'
                    AND completed_at IS NULL
                    AND failure_reason IS NULL
                )
                OR
            (
                status = 'COMPLETED'
                    AND completed_at IS NOT NULL
                    AND failure_reason IS NULL
                )
                OR
            (
                status IN ('REJECTED', 'FAILED')
                    AND completed_at IS NOT NULL
                    AND failure_reason IS NOT NULL
                )
            )
);

CREATE TABLE account_movement
(
    id            UUID PRIMARY KEY,
    account_id    UUID                     NOT NULL,
    transfer_id   UUID,
    movement_type VARCHAR(20)              NOT NULL,
    amount        NUMERIC(19, 2)           NOT NULL,
    currency      VARCHAR(3)               NOT NULL,
    balance_after NUMERIC(19, 2)           NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_account_movement_account
        FOREIGN KEY (account_id)
            REFERENCES bank_account (id),

    CONSTRAINT fk_account_movement_transfer
        FOREIGN KEY (transfer_id)
            REFERENCES bank_transfer (id),

    CONSTRAINT ck_account_movement_type
        CHECK (
            movement_type IN (
                              'DEPOSIT',
                              'DEBIT',
                              'CREDIT'
                )
            ),

    CONSTRAINT ck_account_movement_amount_positive
        CHECK (amount > 0),

    CONSTRAINT ck_account_movement_balance_non_negative
        CHECK (balance_after >= 0),

    CONSTRAINT ck_account_movement_currency
        CHECK (currency IN ('PEN', 'USD')),

    CONSTRAINT ck_account_movement_transfer_reference
        CHECK (
            (
                movement_type = 'DEPOSIT'
                    AND transfer_id IS NULL
                )
                OR
            (
                movement_type IN ('DEBIT', 'CREDIT')
                    AND transfer_id IS NOT NULL
                )
            )
);

CREATE INDEX idx_account_movement_account_created
    ON account_movement (account_id, created_at);

CREATE INDEX idx_bank_transfer_source_created
    ON bank_transfer (source_account_id, created_at);

CREATE INDEX idx_bank_transfer_destination_created
    ON bank_transfer (destination_account_id, created_at);