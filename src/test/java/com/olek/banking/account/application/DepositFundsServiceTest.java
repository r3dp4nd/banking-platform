package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.account.domain.exception.AccountNotActiveException;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.account.domain.exception.CurrencyMismatchException;
import com.olek.banking.account.infrastructure.persistence.InMemoryAccountRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepositFundsServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T12:00:00Z");

    private AccountRepository accountRepository;
    private DepositFundsService service;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        service = new DepositFundsService(accountRepository);
    }

    @Test
    void shouldDepositFundsAndPersistUpdatedAccount() {
        Account account = activeAccount(CurrencyCode.PEN);
        accountRepository.save(account);

        Account result = service.deposit(
                new DepositFundsCommand(
                        account.id(),
                        money("250.00", CurrencyCode.PEN)
                )
        );

        assertThat(result.balance())
                .isEqualTo(
                        money("250.00", CurrencyCode.PEN)
                );

        assertThat(
                accountRepository.findById(account.id())
        )
                .get()
                .extracting(Account::balance)
                .isEqualTo(
                        money("250.00", CurrencyCode.PEN)
                );
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