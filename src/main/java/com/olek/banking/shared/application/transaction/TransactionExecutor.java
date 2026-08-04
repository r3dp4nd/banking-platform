package com.olek.banking.shared.application.transaction;

/**
 * Defines the transaction boundary required by application services.
 */
public interface TransactionExecutor {

    /**
     * Executes an operation inside a transaction.
     *
     * @param action operation to execute
     * @param <T>    result type
     * @return operation result
     */
    <T> T execute(TransactionAction<T> action);
}