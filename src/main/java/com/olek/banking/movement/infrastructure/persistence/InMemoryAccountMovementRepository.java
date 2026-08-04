package com.olek.banking.movement.infrastructure.persistence;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.movement.domain.MovementId;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores immutable account movements in memory.
 *
 * <p>This adapter is intended for local development and isolated tests.
 * Its contents are lost when the application stops.</p>
 */
public final class InMemoryAccountMovementRepository
        implements AccountMovementRepository {

    private final Map<MovementId, AccountMovement> movements =
            new ConcurrentHashMap<>();

    /**
     * Saves a movement.
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

        AccountMovement existing = movements.putIfAbsent(
                movement.id(),
                movement
        );

        if (existing != null) {
            throw new IllegalStateException(
                    "account movement already exists"
            );
        }

        return movement;
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

        return Optional.ofNullable(
                movements.get(movementId)
        );
    }

    /**
     * Finds account movements ordered from oldest to newest.
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

        return movements.values()
                .stream()
                .filter(movement ->
                        movement.accountId().equals(accountId)
                )
                .sorted(
                        Comparator.comparing(
                                AccountMovement::createdAt
                        ).thenComparing(
                                movement ->
                                        movement.id()
                                                .toString()
                        )
                )
                .toList();
    }

    /**
     * Returns all movements.
     *
     * @return immutable movement collection
     */
    @Override
    public List<AccountMovement> findAll() {
        return List.copyOf(movements.values());
    }
}