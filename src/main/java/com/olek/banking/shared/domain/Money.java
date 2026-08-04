package com.olek.banking.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents an immutable monetary amount expressed in a supported currency.
 *
 * <p>Amounts are normalized to two decimal places and cannot be negative.</p>
 *
 * @param amount   monetary amount
 * @param currency currency associated with the amount
 */
public record Money(
        BigDecimal amount,
        CurrencyCode currency
) {

    private static final int SCALE = 2;

    /**
     * Creates and validates a monetary value.
     *
     * @throws NullPointerException     if the amount or currency is {@code null}
     * @throws ArithmeticException      if the amount contains more than two decimal places
     * @throws IllegalArgumentException if the amount is negative
     */
    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    /**
     * Creates and validates a monetary value.
     *
     * @throws NullPointerException     if the amount or currency is {@code null}
     * @throws ArithmeticException      if the amount contains more than two decimal places
     * @throws IllegalArgumentException if the amount is negative
     */
    public static Money zero(CurrencyCode currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Adds another monetary value with the same currency.
     *
     * @param other monetary value to add
     * @return the resulting monetary value
     * @throws IllegalArgumentException if the currencies do not match
     */
    public Money add(Money other) {
        requireSameCurrency(other);

        return new Money(
                amount.add(other.amount),
                currency
        );
    }

    /**
     * Subtracts another monetary value with the same currency.
     *
     * @param other monetary value to subtract
     * @return the resulting monetary value
     * @throws IllegalArgumentException if the currencies do not match or the result is negative
     */
    public Money subtract(Money other) {
        requireSameCurrency(other);

        if (amount.compareTo(other.amount) < 0) {
            throw new IllegalArgumentException("resulting amount must not be negative");
        }

        return new Money(
                amount.subtract(other.amount),
                currency
        );
    }

    /**
     * Checks whether this amount is lower than another amount.
     *
     * @param other monetary value to compare
     * @return {@code true} when this amount is lower
     * @throws IllegalArgumentException if the currencies do not match
     */
    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    /**
     * Checks whether the monetary amount is zero.
     *
     * @return {@code true} when the amount is zero
     */
    public boolean isZero() {
        return amount.signum() == 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "money must not be null");

        if (currency != other.currency) {
            throw new IllegalArgumentException("currencies must match");
        }
    }
}

