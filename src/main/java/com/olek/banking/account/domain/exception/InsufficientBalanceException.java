package com.olek.banking.account.domain.exception;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.shared.domain.Money;

import java.util.Map;

/**
 * Indicates that an account does not contain enough funds.
 */
public class InsufficientBalanceException extends DomainException {

    /**
     * Creates the exception.
     *
     * @param accountId account with insufficient funds
     * @param available current available balance
     * @param requested requested debit amount
     */
    public InsufficientBalanceException(
            AccountId accountId,
            Money available,
            Money requested
    ) {
        super(
                DomainErrorCode.INSUFFICIENT_BALANCE,
                "account has insufficient balance",
                Map.of(
                        "accountId", accountId.toString(),
                        "available", available.amount(),
                        "requested", requested.amount(),
                        "currency", available.currency().name()
                )
        );
    }
}
