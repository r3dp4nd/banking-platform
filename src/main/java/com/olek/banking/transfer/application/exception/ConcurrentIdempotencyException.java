package com.olek.banking.transfer.application.exception;

/**
 * Indicates that another transaction persisted the same idempotency key
 * concurrently.
 */
public final class ConcurrentIdempotencyException
        extends RuntimeException {

    private final String idempotencyKey;

    /**
     * Creates the concurrent idempotency exception.
     *
     * @param idempotencyKey duplicated idempotency key
     * @param cause          persistence exception that detected the collision
     */
    public ConcurrentIdempotencyException(
            String idempotencyKey,
            Throwable cause
    ) {
        super(
                "idempotency key was persisted concurrently",
                cause
        );

        this.idempotencyKey = idempotencyKey;
    }

    /**
     * Returns the duplicated idempotency key.
     *
     * @return duplicated key
     */
    public String idempotencyKey() {
        return idempotencyKey;
    }
}