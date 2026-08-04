package com.olek.banking.account.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a bank account uniquely within the banking platform.
 *
 * @param value unique account identifier
 */
public record AccountId(UUID value) {

    /**
     * Creates and validates an account identifier.
     *
     * @throws NullPointerException if the value is {@code null}
     */
    public AccountId {
        Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Generates a new account identifier.
     *
     * @return newly generated account identifier
     */
    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

    /**
     * Creates an account identifier from its textual representation.
     *
     * @param value UUID textual representation
     * @return parsed account identifier
     * @throws NullPointerException     if the value is {@code null}
     * @throws IllegalArgumentException if the value is not a valid UUID
     */
    public static AccountId from(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return new AccountId(UUID.fromString(value));
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