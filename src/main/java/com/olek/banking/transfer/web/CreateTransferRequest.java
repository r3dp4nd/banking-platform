package com.olek.banking.transfer.web;

import com.olek.banking.shared.domain.CurrencyCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Represents the HTTP request used to create an internal transfer.
 *
 * @param sourceAccountId      account sending the funds
 * @param destinationAccountId account receiving the funds
 * @param amount               monetary amount to transfer
 * @param currency             transfer currency
 * @param description          optional transfer description
 */
public record CreateTransferRequest(

        @NotBlank(message = "sourceAccountId must not be blank")
        String sourceAccountId,

        @NotBlank(message = "destinationAccountId must not be blank")
        String destinationAccountId,

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
        CurrencyCode currency,

        @Size(
                max = 140,
                message = "description must contain at most 140 characters"
        )
        String description
) {
}