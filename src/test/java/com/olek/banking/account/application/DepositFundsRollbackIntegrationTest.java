package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
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
class DepositFundsRollbackIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DepositFundsService service;

    @MockitoBean
    private AccountMovementRepository movementRepository;

    @BeforeEach
    void setUp() {
        reset(movementRepository);
    }

    @Test
    void shouldRollbackBalanceWhenMovementFails() {
        Account account = accountRepository.save(
                Account.open(
                        AccountId.generate(),
                        "001-9000000003",
                        CurrencyCode.PEN,
                        Instant.parse("2026-08-04T17:00:00Z")
                )
        );

        when(
                movementRepository.save(
                        any(AccountMovement.class)
                )
        ).thenThrow(
                new IllegalStateException(
                        "simulated movement failure"
                )
        );

        assertThatThrownBy(() ->
                service.deposit(
                        new DepositFundsCommand(
                                account.id(),
                                money("500.00")
                        )
                )
        )
                .isInstanceOf(IllegalStateException.class);

        Account persisted = accountRepository
                .findById(account.id())
                .orElseThrow();

        assertThat(persisted.balance())
                .isEqualTo(Money.zero(CurrencyCode.PEN));
    }

    private Money money(String amount) {
        return new Money(
                new BigDecimal(amount),
                CurrencyCode.PEN
        );
    }
}
