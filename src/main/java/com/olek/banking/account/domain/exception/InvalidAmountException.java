package com.olek.banking.account.domain.exception;

import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.shared.domain.Money;

import java.util.Map;

/**
 * Indicates that a monetary operation received an invalid amount.
 */
public class InvalidAmountException extends DomainException {

    /**
     * Creates the exception.
     *
     * @param amount invalid monetary amount
     */
    public InvalidAmountException(Money amount) {
        super(
                DomainErrorCode.INVALID_AMOUNT,
                "amount must be greater than zero",
                Map.of(
                        "amount", amount.amount(),
                        "currency", amount.currency().name()
                )
        );
    }
}
