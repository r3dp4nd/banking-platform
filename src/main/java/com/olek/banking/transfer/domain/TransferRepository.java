package com.olek.banking.transfer.domain;

import java.util.List;
import java.util.Optional;

/**
 * Defines the persistence operations required by the transfer domain.
 */
public interface TransferRepository {

    /**
     * Saves a new transfer or updates an existing one.
     *
     * @param transfer transfer to persist
     * @return persisted transfer
     */
    Transfer save(Transfer transfer);

    /**
     * Finds a transfer by its identifier.
     *
     * @param transferId transfer identifier
     * @return transfer when found
     */
    Optional<Transfer> findById(TransferId transferId);

    /**
     * Finds a transfer by its idempotency key.
     *
     * @param idempotencyKey idempotency key
     * @return transfer when found
     */
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    /**
     * Checks whether an idempotency key is already registered.
     *
     * @param idempotencyKey idempotency key
     * @return {@code true} when the key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Returns all persisted transfers.
     *
     * @return immutable transfer collection
     */
    List<Transfer> findAll();
}