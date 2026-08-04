package com.olek.banking.shared.infrastructure.transaction;

import com.olek.banking.shared.application.transaction.TransactionAction;
import com.olek.banking.shared.application.transaction.TransactionExecutor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/**
 * Executes application operations using Spring-managed transactions.
 */
@Component
@Profile("!memory")
public final class SpringTransactionExecutor
        implements TransactionExecutor {

    private final TransactionTemplate transactionTemplate;

    /**
     * Creates the Spring transaction executor.
     *
     * @param transactionManager configured transaction manager
     */
    public SpringTransactionExecutor(
            PlatformTransactionManager transactionManager
    ) {
        Objects.requireNonNull(
                transactionManager,
                "transactionManager must not be null"
        );

        this.transactionTemplate =
                new TransactionTemplate(transactionManager);
    }

    /**
     * Executes an operation inside a Spring transaction.
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

        return transactionTemplate.execute(
                status -> action.execute()
        );
    }
}