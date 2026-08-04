package com.olek.banking.shared.infrastructure.transaction;

import com.olek.banking.shared.application.transaction.TransactionAction;
import com.olek.banking.shared.application.transaction.TransactionExecutor;

import java.util.Objects;

/**
 * Executes operations directly when transactional infrastructure is absent.
 */
public final class DirectTransactionExecutor
        implements TransactionExecutor {

    /**
     * Executes the operation without opening a technical transaction.
     *
     * @param action operation to execute
     * @param <T>    result type
     * @return operation result
     */
    @Override
    public <T> T execute(TransactionAction<T> action) {
        Objects.requireNonNull(
                action,
                "action must not be null"
        );

        return action.execute();
    }
}