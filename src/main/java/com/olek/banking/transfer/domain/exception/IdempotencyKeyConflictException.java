package com.olek.banking.transfer.domain.exception;

import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.transfer.domain.Transfer;

import java.util.Map;

/**
 * Indicates that an idempotency key was reused with different transfer data.
 */
public final class IdempotencyKeyConflictException
        extends DomainException {

    /**
     * Creates the exception.
     *
     * @param existingTransfer transfer previously associated with the key
     */
    public IdempotencyKeyConflictException(
            Transfer existingTransfer
    ) {
        super(
                DomainErrorCode.IDEMPOTENCY_KEY_CONFLICT,
                "idempotency key was already used with different data",
                Map.of(
                        "idempotencyKey",
                        existingTransfer.idempotencyKey(),
                        "existingTransferId",
                        existingTransfer.id().toString()
                )
        );
    }
}