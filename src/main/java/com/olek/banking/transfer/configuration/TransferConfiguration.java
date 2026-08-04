package com.olek.banking.transfer.configuration;

import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.transfer.application.CreateTransferService;
import com.olek.banking.transfer.application.GetTransferService;
import com.olek.banking.transfer.domain.TransferRepository;
import com.olek.banking.transfer.infrastructure.persistence.InMemoryTransferRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configures the transfer module dependencies.
 */
@Configuration
public class TransferConfiguration {

    /**
     * Creates the transfer repository used by the application.
     *
     * @return in-memory transfer repository
     */
    @Bean
    TransferRepository transferRepository() {
        return new InMemoryTransferRepository();
    }

    /**
     * Creates the transfer processing use case.
     *
     * @param accountRepository  account persistence port
     * @param transferRepository transfer persistence port
     * @param clock              source of the current time
     * @return configured transfer service
     */
    @Bean
    CreateTransferService createTransferService(
            AccountRepository accountRepository,
            TransferRepository transferRepository,
            Clock clock
    ) {
        return new CreateTransferService(
                accountRepository,
                transferRepository,
                clock
        );
    }

    /**
     * Creates the transfer query use case.
     *
     * @param transferRepository transfer persistence port
     * @return configured transfer query service
     */
    @Bean
    GetTransferService getTransferService(
            TransferRepository transferRepository
    ) {
        return new GetTransferService(transferRepository);
    }
}