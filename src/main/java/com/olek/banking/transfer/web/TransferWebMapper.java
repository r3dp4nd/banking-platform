package com.olek.banking.transfer.web;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.shared.web.error.InvalidRequestParameterException;
import com.olek.banking.transfer.application.CreateTransferCommand;
import com.olek.banking.transfer.domain.Transfer;

/**
 * Maps transfer web models to application and domain models.
 */
final class TransferWebMapper {

    private TransferWebMapper() {
    }

    /**
     * Maps an HTTP transfer request to an application command.
     *
     * @param request        validated transfer request
     * @param idempotencyKey request idempotency key
     * @return transfer application command
     * @throws InvalidRequestParameterException if an account identifier is
     *                                          not a valid UUID
     */
    static CreateTransferCommand toCommand(
            CreateTransferRequest request,
            String idempotencyKey
    ) {
        return new CreateTransferCommand(
                toAccountId(
                        "sourceAccountId",
                        request.sourceAccountId()
                ),
                toAccountId(
                        "destinationAccountId",
                        request.destinationAccountId()
                ),
                new Money(
                        request.amount(),
                        request.currency()
                ),
                request.description(),
                idempotencyKey
        );
    }

    /**
     * Maps a transfer to its HTTP response representation.
     *
     * @param transfer transfer to map
     * @return transfer response
     */
    static TransferResponse toResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.id().toString(),
                transfer.sourceAccountId().toString(),
                transfer.destinationAccountId().toString(),
                transfer.amount().amount(),
                transfer.amount().currency(),
                transfer.description(),
                transfer.status().name(),
                transfer.createdAt(),
                transfer.completedAt(),
                transfer.failureReason()
        );
    }

    private static AccountId toAccountId(
            String parameter,
            String value
    ) {
        try {
            return AccountId.from(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestParameterException(
                    parameter,
                    value
            );
        }
    }
}