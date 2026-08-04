package com.olek.banking.account.web;

import com.olek.banking.shared.domain.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Represents the HTTP request used to deposit funds into an account.
 *
 * @param amount   monetary amount to deposit
 * @param currency currency of the deposit
 */
public record DepositFundsRequest(

        @NotNull(message = "amount must not be null")
        @DecimalMin(
                value = "0.01",
                message = "amount must be greater than zero"
        )
        @Digits(
                integer = 17,
                fraction = 2,
                message = "amount must contain at most two decimal places"
        )
        BigDecimal amount,

        @NotNull(message = "currency must not be null")
        CurrencyCode currency
) {
}