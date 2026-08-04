package com.olek.banking.account.web;

import com.olek.banking.shared.domain.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Represents the HTTP request used to open a bank account.
 *
 * @param accountNumber externally visible account number
 * @param currency      currency managed by the account
 */
public record OpenAccountRequest(

        @NotBlank(message = "accountNumber must not be blank")
        @Pattern(
                regexp = "\\d{3}-\\d{10}",
                message = "accountNumber must use the format 000-0000000000"
        )
        String accountNumber,

        @NotNull(message = "currency must not be null")
        CurrencyCode currency
) {
}