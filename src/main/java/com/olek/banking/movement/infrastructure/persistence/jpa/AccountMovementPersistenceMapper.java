package com.olek.banking.movement.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.MovementId;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.TransferId;

/**
 * Maps account movement domain models to and from JPA entities.
 */
final class AccountMovementPersistenceMapper {

    private AccountMovementPersistenceMapper() {
    }

    /**
     * Maps a domain movement to its JPA representation.
     *
     * @param movement domain movement
     * @return persistence entity
     */
    static AccountMovementJpaEntity toEntity(
            AccountMovement movement
    ) {
        return new AccountMovementJpaEntity(
                movement.id().value(),
                movement.accountId().value(),
                movement.transferId() == null
                        ? null
                        : movement.transferId().value(),
                movement.type(),
                movement.amount().amount(),
                movement.amount().currency(),
                movement.balanceAfter().amount(),
                movement.createdAt()
        );
    }

    /**
     * Maps a JPA movement entity to its domain representation.
     *
     * @param entity persistence entity
     * @return reconstructed domain movement
     */
    static AccountMovement toDomain(
            AccountMovementJpaEntity entity
    ) {
        TransferId transferId = entity.getTransferId() == null
                ? null
                : new TransferId(entity.getTransferId());

        return new AccountMovement(
                new MovementId(entity.getId()),
                new AccountId(entity.getAccountId()),
                transferId,
                entity.getMovementType(),
                new Money(
                        entity.getAmount(),
                        entity.getCurrency()
                ),
                new Money(
                        entity.getBalanceAfter(),
                        entity.getCurrency()
                ),
                entity.getCreatedAt()
        );
    }
}