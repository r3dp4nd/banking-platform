package com.olek.banking.account.domain.exception;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;

import java.util.Map;

/**
 * Indicates that an account could not be found.
 */
public final class AccountNotFoundException extends DomainException {

    /**
     * Creates the exception.
     *
     * @param accountId missing account identifier
     */
    public AccountNotFoundException(AccountId accountId) {
        super(
                DomainErrorCode.ACCOUNT_NOT_FOUND,
                "account not found",
                Map.of("accountId", accountId.toString())
        );
    }
}