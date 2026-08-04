package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
class CreateTransferRollbackIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private CreateTransferService service;

    @MockitoBean
    private AccountMovementRepository movementRepository;

    @BeforeEach
    void setUp() {
        reset(movementRepository);
    }

    @Test
    void shouldRollbackAccountsAndTransferWhenMovementFails() {
        Account source = account(
                "001-9000000001",
                "500.00"
        );

        Account destination = account(
                "001-9000000002",
                "100.00"
        );

        accountRepository.save(source);
        accountRepository.save(destination);

        when(
                movementRepository.save(
                        any(AccountMovement.class)
                )
        ).thenThrow(
                new IllegalStateException(
                        "simulated movement failure"
                )
        );

        CreateTransferCommand command =
                new CreateTransferCommand(
                        source.id(),
                        destination.id(),
                        money("150.00"),
                        "Rollback test",
                        "rollback-test-001"
                );

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("simulated movement failure");

        Account persistedSource = accountRepository
                .findById(source.id())
                .orElseThrow();

        Account persistedDestination = accountRepository
                .findById(destination.id())
                .orElseThrow();

        assertThat(persistedSource.balance())
                .isEqualTo(money("500.00"));

        assertThat(persistedDestination.balance())
                .isEqualTo(money("100.00"));

        assertThat(
                transferRepository.findByIdempotencyKey(
                        "rollback-test-001"
                )
        ).isEmpty();
    }

    private Account account(
            String accountNumber,
            String initialBalance
    ) {
        return new Account(
                AccountId.generate(),
                accountNumber,
                CurrencyCode.PEN,
                money(initialBalance),
                AccountStatus.ACTIVE,
                Instant.parse("2026-08-04T17:00:00Z")
        );
    }

    private Money money(String amount) {
        return new Money(
                new BigDecimal(amount),
                CurrencyCode.PEN
        );
    }
}