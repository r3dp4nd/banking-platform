package com.olek.banking.transfer.web;

import com.olek.banking.shared.domain.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a money transfer returned by the HTTP API.
 *
 * @param transferId           unique transfer identifier
 * @param sourceAccountId      account that sent the funds
 * @param destinationAccountId account that received the funds
 * @param amount               transferred amount
 * @param currency             transfer currency
 * @param description          transfer description
 * @param status               current transfer status
 * @param createdAt            creation timestamp
 * @param completedAt          completion timestamp, when available
 * @param failureReason        safe rejection or failure reason, when available
 */
public record TransferResponse(
        String transferId,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        CurrencyCode currency,
        String description,
        String status,
        Instant createdAt,
        Instant completedAt,
        String failureReason
) {
}