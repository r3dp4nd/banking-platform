package com.olek.banking.transfer.application;

import com.olek.banking.transfer.domain.Transfer;

import java.util.Objects;

/**
 * Represents the result of creating or recovering an idempotent transfer.
 *
 * @param transfer resulting transfer
 * @param outcome  transfer processing outcome
 */
public record CreateTransferResult(
        Transfer transfer,
        Outcome outcome
) {

    /**
     * Creates and validates the result.
     */
    public CreateTransferResult {
        Objects.requireNonNull(
                transfer,
                "transfer must not be null"
        );

        Objects.requireNonNull(
                outcome,
                "outcome must not be null"
        );
    }

    /**
     * Creates a result for a newly processed transfer.
     *
     * @param transfer newly processed transfer
     * @return created result
     */
    public static CreateTransferResult created(
            Transfer transfer
    ) {
        return new CreateTransferResult(
                transfer,
                Outcome.CREATED
        );
    }

    /**
     * Creates a result for an idempotently recovered transfer.
     *
     * @param transfer previously processed transfer
     * @return recovered result
     */
    public static CreateTransferResult recovered(
            Transfer transfer
    ) {
        return new CreateTransferResult(
                transfer,
                Outcome.RECOVERED
        );
    }

    /**
     * Indicates whether the transfer was processed for the first time.
     *
     * @return {@code true} for a newly processed transfer
     */
    public boolean wasCreated() {
        return outcome == Outcome.CREATED;
    }

    /**
     * Describes how the transfer result was obtained.
     */
    public enum Outcome {

        /**
         * The request created and processed a new transfer.
         */
        CREATED,

        /**
         * The request returned a previously processed transfer.
         */
        RECOVERED
    }
}