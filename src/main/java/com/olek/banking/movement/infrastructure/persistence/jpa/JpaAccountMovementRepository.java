package com.olek.banking.movement.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.movement.domain.MovementId;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Persists immutable account movements through Spring Data JPA.
 */
@Repository
@Profile("!memory")
public class JpaAccountMovementRepository
        implements AccountMovementRepository {

    private final SpringDataAccountMovementJpaRepository jpaRepository;

    /**
     * Creates the JPA account movement repository adapter.
     *
     * @param jpaRepository Spring Data movement repository
     */
    public JpaAccountMovementRepository(
            SpringDataAccountMovementJpaRepository jpaRepository
    ) {
        this.jpaRepository = Objects.requireNonNull(
                jpaRepository,
                "jpaRepository must not be null"
        );
    }

    /**
     * Inserts an immutable account movement.
     *
     * @param movement movement to persist
     * @return persisted movement
     * @throws IllegalStateException if the movement already exists
     */
    @Override
    public AccountMovement save(AccountMovement movement) {
        Objects.requireNonNull(
                movement,
                "movement must not be null"
        );

        if (jpaRepository.existsById(movement.id().value())) {
            throw new IllegalStateException(
                    "account movement already exists"
            );
        }

        try {
            AccountMovementJpaEntity savedEntity =
                    jpaRepository.saveAndFlush(
                            AccountMovementPersistenceMapper.toEntity(
                                    movement
                            )
                    );

            return AccountMovementPersistenceMapper.toDomain(
                    savedEntity
            );
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException(
                    "account movement could not be persisted",
                    exception
            );
        }
    }

    /**
     * Finds a movement by its identifier.
     *
     * @param movementId movement identifier
     * @return movement when found
     */
    @Override
    public Optional<AccountMovement> findById(
            MovementId movementId
    ) {
        Objects.requireNonNull(
                movementId,
                "movementId must not be null"
        );

        return jpaRepository
                .findById(movementId.value())
                .map(AccountMovementPersistenceMapper::toDomain);
    }

    /**
     * Finds account movements in chronological order.
     *
     * @param accountId account identifier
     * @return immutable ordered movement collection
     */
    @Override
    public List<AccountMovement> findByAccountId(
            AccountId accountId
    ) {
        Objects.requireNonNull(
                accountId,
                "accountId must not be null"
        );

        return jpaRepository
                .findByAccountIdOrderByCreatedAtAscIdAsc(
                        accountId.value()
                )
                .stream()
                .map(AccountMovementPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Returns all persisted movements.
     *
     * @return immutable movement collection
     */
    @Override
    public List<AccountMovement> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(AccountMovementPersistenceMapper::toDomain)
                .toList();
    }
}