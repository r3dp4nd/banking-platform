package com.olek.banking.account.domain.exception;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;

import java.util.Map;

/**
 * Indicates that an operation requires an active account.
 */
public class AccountNotActiveException extends DomainException {

    /**
     * Creates the exception.
     *
     * @param accountId account that rejected the operation
     * @param status    current account status
     */
    public AccountNotActiveException(
            AccountId accountId,
            AccountStatus status
    ) {
        super(
                DomainErrorCode.ACCOUNT_NOT_ACTIVE,
                "account must be active",
                Map.of(
                        "accountId", accountId.toString(),
                        "status", status.name()
                )
        );
    }
}
