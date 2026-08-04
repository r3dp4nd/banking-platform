package com.olek.banking.movement.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies an account movement uniquely within the banking platform.
 *
 * @param value unique movement identifier
 */
public record MovementId(UUID value) {

    /**
     * Creates and validates a movement identifier.
     *
     * @throws NullPointerException if the value is {@code null}
     */
    public MovementId {
        Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Generates a new movement identifier.
     *
     * @return newly generated movement identifier
     */
    public static MovementId generate() {
        return new MovementId(UUID.randomUUID());
    }

    /**
     * Creates a movement identifier from its textual representation.
     *
     * @param value UUID textual representation
     * @return parsed movement identifier
     * @throws NullPointerException     if the value is {@code null}
     * @throws IllegalArgumentException if the value is not a valid UUID
     */
    public static MovementId from(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return new MovementId(UUID.fromString(value));
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