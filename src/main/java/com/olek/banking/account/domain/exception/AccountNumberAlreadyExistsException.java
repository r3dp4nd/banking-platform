package com.olek.banking.account.domain.exception;

import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;

import java.util.Map;

/**
 * Indicates that an account number is already registered.
 */
public final class AccountNumberAlreadyExistsException extends DomainException {

    /**
     * Creates the exception.
     *
     * @param accountNumber duplicated account number
     */
    public AccountNumberAlreadyExistsException(
            String accountNumber
    ) {
        super(
                DomainErrorCode.ACCOUNT_NUMBER_ALREADY_EXISTS,
                "account number already exists",
                Map.of("accountNumber", accountNumber)
        );
    }
}