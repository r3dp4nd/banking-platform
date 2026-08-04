package com.olek.banking.account.domain.exception;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;

import java.util.Map;

/**
 * Indicates that a closed account cannot change its operational status.
 */
public final class AccountAlreadyClosedException extends DomainException {

    /**
     * Creates the exception.
     *
     * @param accountId closed account identifier
     */
    public AccountAlreadyClosedException(AccountId accountId) {
        super(
                DomainErrorCode.ACCOUNT_ALREADY_CLOSED,
                "closed account cannot change status",
                Map.of("accountId", accountId.toString())
        );
    }
}