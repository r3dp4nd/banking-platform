package com.olek.banking.account.application;

import com.olek.banking.shared.domain.CurrencyCode;

import java.util.Objects;

/**
 * Contains the information required to open a bank account.
 *
 * @param accountNumber externally visible account number
 * @param currency      currency managed by the account
 */
public record OpenAccountCommand(
        String accountNumber,
        CurrencyCode currency
) {

    /**
     * Creates and validates an account opening command.
     *
     * @throws NullPointerException     if the account number or currency is
     *                                  {@code null}
     * @throws IllegalArgumentException if the account number is blank
     */
    public OpenAccountCommand {
        Objects.requireNonNull(
                accountNumber,
                "accountNumber must not be null"
        );

        Objects.requireNonNull(
                currency,
                "currency must not be null"
        );

        accountNumber = accountNumber.trim();

        if (accountNumber.isEmpty()) {
            throw new IllegalArgumentException(
                    "accountNumber must not be blank"
            );
        }
    }
}