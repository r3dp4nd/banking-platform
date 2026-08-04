package com.olek.banking.transfer.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides Spring Data JPA operations for transfer persistence entities.
 */
public interface SpringDataTransferJpaRepository
        extends JpaRepository<TransferJpaEntity, UUID> {

    /**
     * Finds a transfer entity by its idempotency key.
     *
     * @param idempotencyKey idempotency key
     * @return transfer entity when found
     */
    Optional<TransferJpaEntity> findByIdempotencyKey(
            String idempotencyKey
    );

    /**
     * Checks whether an idempotency key is already persisted.
     *
     * @param idempotencyKey idempotency key
     * @return {@code true} when the key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);
}