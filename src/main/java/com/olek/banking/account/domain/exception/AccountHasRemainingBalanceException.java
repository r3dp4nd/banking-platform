package com.olek.banking.account.domain.exception;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.shared.domain.Money;

import java.util.Map;

/**
 * Indicates that an account cannot be closed while it contains funds.
 */
public final class AccountHasRemainingBalanceException extends DomainException {

    /**
     * Creates the exception.
     *
     * @param accountId account that cannot be closed
     * @param balance   remaining account balance
     */
    public AccountHasRemainingBalanceException(
            AccountId accountId,
            Money balance
    ) {
        super(
                DomainErrorCode.ACCOUNT_HAS_REMAINING_BALANCE,
                "account with remaining balance cannot be closed",
                Map.of(
                        "accountId", accountId.toString(),
                        "balance", balance.amount(),
                        "currency", balance.currency().name()
                )
        );
    }
}