package com.olek.banking.movement.configuration;

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
}