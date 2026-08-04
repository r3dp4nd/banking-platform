package com.olek.banking.transfer.infrastructure.persistence;

import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores transfers in memory.
 *
 * <p>This adapter is intended for local development and isolated tests.
 * Its contents are lost when the application stops.</p>
 */
public final class InMemoryTransferRepository
        implements TransferRepository {

    private final Map<TransferId, Transfer> transfers =
            new ConcurrentHashMap<>();

    /**
     * Saves a transfer or replaces the existing transfer with the same ID.
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

        transfers.put(transfer.id(), transfer);
        return transfer;
    }

    /**
     * Finds a transfer by its identifier.
     *
     * @param transferId transfer identifier
     * @return transfer when found
     */
    @Override
    public Optional<Transfer> findById(TransferId transferId) {
        Objects.requireNonNull(
                transferId,
                "transferId must not be null"
        );

        return Optional.ofNullable(transfers.get(transferId));
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

        return transfers.values()
                .stream()
                .filter(transfer ->
                        transfer.idempotencyKey().equals(normalized)
                )
                .findFirst();
    }

    /**
     * Checks whether an idempotency key already exists.
     *
     * @param idempotencyKey idempotency key
     * @return {@code true} when the key exists
     */
    @Override
    public boolean existsByIdempotencyKey(
            String idempotencyKey
    ) {
        return findByIdempotencyKey(idempotencyKey).isPresent();
    }

    /**
     * Returns all stored transfers.
     *
     * @return immutable transfer collection
     */
    @Override
    public List<Transfer> findAll() {
        return List.copyOf(transfers.values());
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