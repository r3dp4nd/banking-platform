package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.account.domain.exception.AccountNotActiveException;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.account.domain.exception.CurrencyMismatchException;
import com.olek.banking.account.infrastructure.persistence.InMemoryAccountRepository;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.movement.domain.MovementType;
import com.olek.banking.movement.infrastructure.persistence.InMemoryAccountMovementRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.shared.infrastructure.MutableClock;
import com.olek.banking.shared.infrastructure.transaction.DirectTransactionExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepositFundsServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T12:00:00Z");

    MutableClock clock = new MutableClock(
            CREATED_AT,
            ZoneOffset.UTC
    );

    private AccountRepository accountRepository;
    private AccountMovementRepository movementRepository;
    private DepositFundsService service;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        movementRepository =
                new InMemoryAccountMovementRepository();

        service = new DepositFundsService(
                accountRepository,
                movementRepository,
                new DirectTransactionExecutor(),
                clock
        );
    }

    @Test
    void shouldDepositFundsAndRecordMovement() {
        Account account = activeAccount(CurrencyCode.PEN);
        accountRepository.save(account);

        Money depositAmount = money(
                "250.00",
                CurrencyCode.PEN
        );

        Account result = service.deposit(
                new DepositFundsCommand(
                        account.id(),
                        depositAmount
                )
        );

        assertThat(result.balance())
                .isEqualTo(
                        money("250.00", CurrencyCode.PEN)
                );

        assertThat(
                movementRepository.findByAccountId(account.id())
        )
                .singleElement()
                .satisfies(movement -> {
                    assertThat(movement.type())
                            .isEqualTo(MovementType.DEPOSIT);

                    assertThat(movement.amount())
                            .isEqualTo(depositAmount);

                    assertThat(movement.balanceAfter())
                            .isEqualTo(
                                    money(
                                            "250.00",
                                            CurrencyCode.PEN
                                    )
                            );

                    assertThat(movement.transferId())
                            .isNull();

                    assertThat(movement.createdAt())
                            .isEqualTo(CREATED_AT);
                });
    }

    @Test
    void shouldAccumulateMultipleDeposits() {
        Account account = activeAccount(CurrencyCode.PEN);
        accountRepository.save(account);

        service.deposit(
                new DepositFundsCommand(
                        account.id(),
                        money("100.00", CurrencyCode.PEN)
                )
        );

        Account result = service.deposit(
                new DepositFundsCommand(
                        account.id(),
                        money("50.00", CurrencyCode.PEN)
                )
        );

        assertThat(result.balance())
                .isEqualTo(
                        money("150.00", CurrencyCode.PEN)
                );
    }

    @Test
    void shouldRejectMissingAccount() {
        AccountId accountId = AccountId.generate();

        DepositFundsCommand command =
                new DepositFundsCommand(
                        accountId,
                        money("100.00", CurrencyCode.PEN)
                );

        assertThatThrownBy(() -> service.deposit(command))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("account not found");
    }

    @Test
    void shouldRejectDepositWithDifferentCurrency() {
        Account account = activeAccount(CurrencyCode.PEN);
        accountRepository.save(account);

        DepositFundsCommand command =
                new DepositFundsCommand(
                        account.id(),
                        money("100.00", CurrencyCode.USD)
                );

        assertThatThrownBy(() -> service.deposit(command))
                .isInstanceOf(CurrencyMismatchException.class)
                .hasMessage("currencies must match");
    }

    @Test
    void shouldRejectDepositIntoBlockedAccount() {
        Account account = activeAccount(CurrencyCode.PEN);
        account.block();
        accountRepository.save(account);

        DepositFundsCommand command =
                new DepositFundsCommand(
                        account.id(),
                        money("100.00", CurrencyCode.PEN)
                );

        assertThatThrownBy(() -> service.deposit(command))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessage("account must be active");
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(() -> service.deposit(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("command must not be null");
    }

    @Test
    void shouldRecordOneMovementForEachDeposit() {
        Account account = activeAccount(CurrencyCode.PEN);
        accountRepository.save(account);

        service.deposit(
                new DepositFundsCommand(
                        account.id(),
                        money("100.00", CurrencyCode.PEN)
                )
        );

        clock.advance(Duration.ofSeconds(1));

        service.deposit(
                new DepositFundsCommand(
                        account.id(),
                        money("50.00", CurrencyCode.PEN)
                )
        );

        assertThat(
                movementRepository.findByAccountId(account.id())
        )
                .hasSize(2)
                .extracting(AccountMovement::balanceAfter)
                .containsExactly(
                        money("100.00", CurrencyCode.PEN),
                        money("150.00", CurrencyCode.PEN)
                );
    }

    private Account activeAccount(CurrencyCode currency) {
        return new Account(
                AccountId.generate(),
                "001-1234567890",
                currency,
                Money.zero(currency),
                AccountStatus.ACTIVE,
                CREATED_AT
        );
    }

    private Money money(
            String amount,
            CurrencyCode currency
    ) {
        return new Money(
                new BigDecimal(amount),
                currency
        );
    }
}