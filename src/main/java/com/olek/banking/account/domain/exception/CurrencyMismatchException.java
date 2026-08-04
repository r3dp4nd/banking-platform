package com.olek.banking.account.domain.exception;

import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;

import java.util.Map;

/**
 * Indicates that a monetary operation uses incompatible currencies.
 */
public class CurrencyMismatchException extends DomainException {

    /**
     * Creates the exception.
     *
     * @param expected expected currency
     * @param actual   received currency
     */
    public CurrencyMismatchException(
            CurrencyCode expected,
            CurrencyCode actual
    ) {
        super(
                DomainErrorCode.CURRENCY_MISMATCH,
                "currencies must match",
                Map.of(
                        "expectedCurrency", expected.name(),
                        "actualCurrency", actual.name()
                )
        );
    }
}
