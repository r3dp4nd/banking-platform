package com.olek.banking.movement.web;

import com.olek.banking.shared.domain.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents an account movement returned by the HTTP API.
 *
 * @param movementId unique movement identifier
 * @param accountId affected account identifier
 * @param transferId related transfer identifier, when available
 * @param type movement type
 * @param amount movement amount
 * @param currency movement currency
 * @param balanceAfter resulting account balance
 * @param createdAt movement creation timestamp
 */
public record AccountMovementResponse(
        String movementId,
        String accountId,
        String transferId,
        String type,
        BigDecimal amount,
        CurrencyCode currency,
        BigDecimal balanceAfter,
        Instant createdAt
) {
}