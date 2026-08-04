package com.olek.banking.account.configuration;

import com.olek.banking.account.application.DepositFundsService;
import com.olek.banking.account.application.GetAccountService;
import com.olek.banking.account.application.OpenAccountService;
import com.olek.banking.account.domain.AccountLockRepository;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.infrastructure.persistence.InMemoryAccountRepository;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.shared.application.transaction.TransactionExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;

/**
 * Configures the account module dependencies.
 */
@Configuration
public class AccountConfiguration {

    /**
     * Creates the shared in-memory account repository instance.
     *
     * @return in-memory account repository
     */
    @Bean
    @Profile("memory")
    InMemoryAccountRepository inMemoryAccountRepository() {
        return new InMemoryAccountRepository();
    }

    /**
     * Exposes the in-memory repository through the account persistence port.
     *
     * @param inMemoryAccountRepository in-memory repository
     * @return account repository port
     */
    @Bean
    @Profile("memory")
    AccountRepository accountRepository(
            InMemoryAccountRepository inMemoryAccountRepository
    ) {
        return inMemoryAccountRepository;
    }

    /**
     * Exposes the in-memory repository through the locking port.
     *
     * @param inMemoryAccountRepository in-memory repository
     * @return account lock repository port
     */
    @Bean
    @Profile("memory")
    AccountLockRepository accountLockRepository(
            InMemoryAccountRepository inMemoryAccountRepository
    ) {
        return inMemoryAccountRepository;
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
     * @param accountRepository   account persistence port
     * @param movementRepository  movement persistence port
     * @param transactionExecutor transaction boundary
     * @param clock               source of the current time
     * @return configured deposit service
     */
    @Bean
    DepositFundsService depositFundsService(
            AccountRepository accountRepository,
            AccountMovementRepository movementRepository,
            TransactionExecutor transactionExecutor,
            Clock clock
    ) {
        return new DepositFundsService(
                accountRepository,
                movementRepository,
                transactionExecutor,
                clock
        );
    }
}
