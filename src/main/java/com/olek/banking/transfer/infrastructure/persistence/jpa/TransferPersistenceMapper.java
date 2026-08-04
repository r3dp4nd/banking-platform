package com.olek.banking.transfer.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;

/**
 * Maps transfer domain models to and from JPA persistence entities.
 */
final class TransferPersistenceMapper {

    private TransferPersistenceMapper() {
    }

    /**
     * Maps a transfer domain entity to its JPA representation.
     *
     * @param transfer domain transfer
     * @return transfer persistence entity
     */
    static TransferJpaEntity toEntity(Transfer transfer) {
        return new TransferJpaEntity(
                transfer.id().value(),
                transfer.sourceAccountId().value(),
                transfer.destinationAccountId().value(),
                transfer.amount().amount(),
                transfer.amount().currency(),
                transfer.description(),
                transfer.idempotencyKey(),
                transfer.status(),
                transfer.failureReason(),
                transfer.createdAt(),
                transfer.completedAt()
        );
    }

    /**
     * Maps a transfer JPA entity to its domain representation.
     *
     * @param entity transfer persistence entity
     * @return reconstructed domain transfer
     */
    static Transfer toDomain(TransferJpaEntity entity) {
        return new Transfer(
                new TransferId(entity.getId()),
                new AccountId(entity.getSourceAccountId()),
                new AccountId(entity.getDestinationAccountId()),
                new Money(
                        entity.getAmount(),
                        entity.getCurrency()
                ),
                entity.getDescription(),
                entity.getIdempotencyKey(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCompletedAt(),
                entity.getFailureReason()
        );
    }
}