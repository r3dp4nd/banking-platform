package com.olek.banking.transfer.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a money transfer uniquely within the banking platform.
 *
 * @param value unique transfer identifier
 */
public record TransferId(UUID value) {

    /**
     * Creates and validates a transfer identifier.
     *
     * @throws NullPointerException if the value is {@code null}
     */
    public TransferId {
        Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Generates a new transfer identifier.
     *
     * @return newly generated transfer identifier
     */
    public static TransferId generate() {
        return new TransferId(UUID.randomUUID());
    }

    /**
     * Creates a transfer identifier from its textual representation.
     *
     * @param value UUID textual representation
     * @return parsed transfer identifier
     * @throws NullPointerException     if the value is {@code null}
     * @throws IllegalArgumentException if the value is not a valid UUID
     */
    public static TransferId from(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return new TransferId(UUID.fromString(value));
    }

    /**
     * Returns the textual representation of the identifier.
     *
     * @return UUID textual representation
     */
    @Override
    public String toString() {
        return value.toString();
    }
}