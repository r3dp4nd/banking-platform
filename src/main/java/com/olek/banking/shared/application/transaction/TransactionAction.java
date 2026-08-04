package com.olek.banking.shared.application.transaction;

/**
 * Represents an operation executed inside a transaction.
 *
 * @param <T> operation result type
 */
@FunctionalInterface
public interface TransactionAction<T> {

    /**
     * Executes the transactional operation.
     *
     * @return operation result
     */
    T execute();
}