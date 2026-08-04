package com.olek.banking.transfer.infrastructure.persistence.jpa;

import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Persists transfer domain entities through Spring Data JPA.
 */
@Repository
@Profile("!memory")
public class JpaTransferRepository
        implements TransferRepository {

    private final SpringDataTransferJpaRepository jpaRepository;

    /**
     * Creates the JPA transfer repository adapter.
     *
     * @param jpaRepository Spring Data transfer repository
     */
    public JpaTransferRepository(
            SpringDataTransferJpaRepository jpaRepository
    ) {
        this.jpaRepository = Objects.requireNonNull(
                jpaRepository,
                "jpaRepository must not be null"
        );
    }

    /**
     * Saves a domain transfer.
     *
     * @param transfer transfer to persist
     * @return persisted transfer
     */
    @Override
    public Transfer save(Transfer transfer) {
        Objects.requireNonNull(
                transfer,
                "transfer must not be null"
        );

        TransferJpaEntity savedEntity = jpaRepository.save(
                TransferPersistenceMapper.toEntity(transfer)
        );

        return TransferPersistenceMapper.toDomain(savedEntity);
    }

    /**
     * Finds a transfer by its identifier.
     *
     * @param transferId transfer identifier
     * @return transfer when found
     */
    @Override
    public Optional<Transfer> findById(
            TransferId transferId
    ) {
        Objects.requireNonNull(
                transferId,
                "transferId must not be null"
        );

        return jpaRepository
                .findById(transferId.value())
                .map(TransferPersistenceMapper::toDomain);
    }

    /**
     * Finds a transfer by its idempotency key.
     *
     * @param idempotencyKey idempotency key
     * @return transfer when found
     */
    @Override
    public Optional<Transfer> findByIdempotencyKey(
            String idempotencyKey
    ) {
        String normalized = normalizeIdempotencyKey(
                idempotencyKey
        );

        return jpaRepository
                .findByIdempotencyKey(normalized)
                .map(TransferPersistenceMapper::toDomain);
    }

    /**
     * Checks whether an idempotency key is persisted.
     *
     * @param idempotencyKey idempotency key
     * @return {@code true} when the key exists
     */
    @Override
    public boolean existsByIdempotencyKey(
            String idempotencyKey
    ) {
        return jpaRepository.existsByIdempotencyKey(
                normalizeIdempotencyKey(idempotencyKey)
        );
    }

    /**
     * Returns all persisted transfers.
     *
     * @return immutable transfer collection
     */
    @Override
    public List<Transfer> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(TransferPersistenceMapper::toDomain)
                .toList();
    }

    private String normalizeIdempotencyKey(String value) {
        Objects.requireNonNull(
                value,
                "idempotencyKey must not be null"
        );

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "idempotencyKey must not be blank"
            );
        }

        return normalized;
    }
}