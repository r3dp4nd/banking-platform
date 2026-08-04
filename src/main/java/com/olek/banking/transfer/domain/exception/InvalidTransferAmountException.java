package com.olek.banking.transfer.domain.exception;

import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.shared.domain.Money;

import java.util.Map;

/**
 * Indicates that a transfer amount is not valid.
 */
public final class InvalidTransferAmountException
        extends DomainException {

    /**
     * Creates the exception.
     *
     * @param amount invalid transfer amount
     */
    public InvalidTransferAmountException(Money amount) {
        super(
                DomainErrorCode.INVALID_TRANSFER_AMOUNT,
                "transfer amount must be greater than zero",
                Map.of(
                        "amount", amount.amount(),
                        "currency", amount.currency().name()
                )
        );
    }
}