package com.olek.banking.account.application;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.Money;

import java.util.Objects;

/**
 * Contains the information required to deposit funds into an account.
 *
 * @param accountId account receiving the funds
 * @param amount    monetary amount to deposit
 */
public record DepositFundsCommand(
        AccountId accountId,
        Money amount
) {

    /**
     * Creates and validates a deposit command.
     *
     * @throws NullPointerException if the account identifier or amount is
     *                              {@code null}
     */
    public DepositFundsCommand {
        Objects.requireNonNull(
                accountId,
                "accountId must not be null"
        );

        Objects.requireNonNull(
                amount,
                "amount must not be null"
        );
    }
}