package com.olek.banking.transfer.domain.exception;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;

import java.util.Map;

/**
 * Indicates that a transfer uses the same source and destination account.
 */
public final class TransferAccountsMustDifferException
        extends DomainException {

    /**
     * Creates the exception.
     *
     * @param accountId account used as both source and destination
     */
    public TransferAccountsMustDifferException(AccountId accountId) {
        super(
                DomainErrorCode.TRANSFER_ACCOUNTS_MUST_DIFFER,
                "source and destination accounts must be different",
                Map.of("accountId", accountId.toString())
        );
    }
}