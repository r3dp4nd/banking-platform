package com.olek.banking.account.configuration;

import com.olek.banking.account.application.DepositFundsService;
import com.olek.banking.account.application.GetAccountService;
import com.olek.banking.account.application.OpenAccountService;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.infrastructure.persistence.InMemoryAccountRepository;
import com.olek.banking.movement.domain.AccountMovementRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configures the account module dependencies.
 */
@Configuration
public class AccountConfiguration {

    /**
     * Creates the account repository used by the application.
     *
     * @return in-memory account repository
     */
    @Bean
    AccountRepository accountRepository() {
        return new InMemoryAccountRepository();
    }

    /**
     * Creates the UTC clock used by account use cases.
     *
     * @return system clock in UTC
     */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Creates the account opening use case.
     *
     * @param accountRepository account persistence port
     * @param clock             source of the current time
     * @return configured account opening service
     */
    @Bean
    OpenAccountService openAccountService(
            AccountRepository accountRepository,
            Clock clock
    ) {
        return new OpenAccountService(
                accountRepository,
                clock
        );
    }

    /**
     * Creates the account query use case.
     *
     * @param accountRepository account persistence port
     * @return configured account query service
     */
    @Bean
    GetAccountService getAccountService(
            AccountRepository accountRepository
    ) {
        return new GetAccountService(accountRepository);
    }

    /**
     * Creates the deposit funds use case.
     *
     * @param accountRepository account persistence port
     * @return configured deposit funds service
     */
    @Bean
    DepositFundsService depositFundsService(
            AccountRepository accountRepository,
            AccountMovementRepository movementRepository,
            Clock clock
    ) {
        return new DepositFundsService(
                accountRepository,
                movementRepository,
                clock
        );
    }
}
