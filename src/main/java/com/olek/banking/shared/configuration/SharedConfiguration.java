package com.olek.banking.shared.configuration;

import com.olek.banking.shared.application.transaction.TransactionExecutor;
import com.olek.banking.shared.infrastructure.transaction.DirectTransactionExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configures shared application dependencies.
 */
@Configuration
public class SharedConfiguration {

    /**
     * Creates the direct transaction executor for in-memory execution.
     *
     * @return direct transaction executor
     */
    @Bean
    @Profile("memory")
    TransactionExecutor transactionExecutor() {
        return new DirectTransactionExecutor();
    }
}