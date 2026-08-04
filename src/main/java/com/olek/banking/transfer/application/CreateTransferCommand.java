package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.exception.InvalidIdempotencyKeyException;

import java.util.Objects;

/**
 * Contains the information required to create an internal money transfer.
 *
 * @param sourceAccountId      account sending the funds
 * @param destinationAccountId account receiving the funds
 * @param amount               monetary amount to transfer
 * @param description          optional transfer description
 * @param idempotencyKey       key used to prevent duplicated processing
 */
public record CreateTransferCommand(
        AccountId sourceAccountId,
        AccountId destinationAccountId,
        Money amount,
        String description,
        String idempotencyKey
) {

    /**
     * Creates and validates a transfer command.
     *
     * @throws NullPointerException           if an account identifier or amount is
     *                                        {@code null}
     * @throws InvalidIdempotencyKeyException if the idempotency key is blank
     */
    public CreateTransferCommand {
        Objects.requireNonNull(
                sourceAccountId,
                "sourceAccountId must not be null"
        );

        Objects.requireNonNull(
                destinationAccountId,
                "destinationAccountId must not be null"
        );

        Objects.requireNonNull(
                amount,
                "amount must not be null"
        );

        description = description == null
                ? ""
                : description.trim();

        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {
            throw new InvalidIdempotencyKeyException();
        }

        idempotencyKey = idempotencyKey.trim();
    }
}