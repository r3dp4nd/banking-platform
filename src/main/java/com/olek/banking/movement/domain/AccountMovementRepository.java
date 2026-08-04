package com.olek.banking.movement.domain;

import com.olek.banking.account.domain.AccountId;

import java.util.List;
import java.util.Optional;

/**
 * Defines persistence operations required by the movement domain.
 */
public interface AccountMovementRepository {

    /**
     * Saves an immutable account movement.
     *
     * @param movement movement to persist
     * @return persisted movement
     */
    AccountMovement save(AccountMovement movement);

    /**
     * Finds a movement by its identifier.
     *
     * @param movementId movement identifier
     * @return movement when found
     */
    Optional<AccountMovement> findById(MovementId movementId);

    /**
     * Finds all movements associated with an account.
     *
     * @param accountId account identifier
     * @return immutable movements ordered from oldest to newest
     */
    List<AccountMovement> findByAccountId(AccountId accountId);

    /**
     * Returns every persisted movement.
     *
     * @return immutable movement collection
     */
    List<AccountMovement> findAll();
}