package com.olek.banking.account.web;

import com.olek.banking.shared.domain.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents the HTTP response returned after opening an account.
 *
 * @param accountId     unique account identifier
 * @param accountNumber externally visible account number
 * @param currency      account currency
 * @param balance       initial account balance
 * @param status        initial account status
 * @param createdAt     account creation timestamp
 */
public record AccountResponse(
        String accountId,
        String accountNumber,
        CurrencyCode currency,
        BigDecimal balance,
        String status,
        Instant createdAt
) {
}