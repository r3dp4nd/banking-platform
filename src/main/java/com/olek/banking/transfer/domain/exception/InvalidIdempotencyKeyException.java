package com.olek.banking.transfer.domain.exception;

import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;

import java.util.Map;

/**
 * Indicates that an idempotency key is missing or invalid.
 */
public final class InvalidIdempotencyKeyException
        extends DomainException {

    /**
     * Creates the exception.
     */
    public InvalidIdempotencyKeyException() {
        super(
                DomainErrorCode.INVALID_IDEMPOTENCY_KEY,
                "idempotency key must not be blank",
                Map.of()
        );
    }
}