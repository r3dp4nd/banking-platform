package com.olek.banking.movement.web;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.shared.web.error.InvalidRequestParameterException;

import java.util.List;

/**
 * Maps account movement domain models to HTTP response models.
 */
final class AccountMovementWebMapper {

    private AccountMovementWebMapper() {
    }

    /**
     * Maps a textual account identifier to its domain representation.
     *
     * @param value textual account identifier
     * @return parsed account identifier
     * @throws InvalidRequestParameterException if the value is not a UUID
     */
    static AccountId toAccountId(String value) {
        try {
            return AccountId.from(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestParameterException(
                    "accountId",
                    value
            );
        }
    }

    /**
     * Maps account movements to their HTTP response representations.
     *
     * @param movements movements to map
     * @return immutable movement responses
     */
    static List<AccountMovementResponse> toResponses(
            List<AccountMovement> movements
    ) {
        return movements.stream()
                .map(AccountMovementWebMapper::toResponse)
                .toList();
    }

    private static AccountMovementResponse toResponse(
            AccountMovement movement
    ) {
        String transferId = movement.transferId() == null
                ? null
                : movement.transferId().toString();

        return new AccountMovementResponse(
                movement.id().toString(),
                movement.accountId().toString(),
                transferId,
                movement.type().name(),
                movement.amount().amount(),
                movement.amount().currency(),
                movement.balanceAfter().amount(),
                movement.createdAt()
        );
    }
}