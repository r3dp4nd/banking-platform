package com.olek.banking.movement.configuration;

import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.movement.application.GetAccountMovementsService;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.movement.infrastructure.persistence.InMemoryAccountMovementRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the account movement module dependencies.
 */
@Configuration
public class MovementConfiguration {

    /**
     * Creates the account movement repository.
     *
     * @return in-memory account movement repository
     */
    @Bean
    AccountMovementRepository accountMovementRepository() {
        return new InMemoryAccountMovementRepository();
    }

    /**
     * Creates the account movement query use case.
     *
     * @param accountRepository account persistence port
     * @param movementRepository account movement persistence port
     * @return configured movement query service
     */
    @Bean
    GetAccountMovementsService getAccountMovementsService(
            AccountRepository accountRepository,
            AccountMovementRepository movementRepository
    ) {
        return new GetAccountMovementsService(
                accountRepository,
                movementRepository
        );
    }
}