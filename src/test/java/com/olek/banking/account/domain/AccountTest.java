package com.olek.banking.account.domain;

import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T12:00:00Z");

    @Test
    void shouldOpenActiveAccountWithZeroBalance() {
        Account account = Account.open(
                new AccountId(UUID.randomUUID()),
                "001-1234567890",
                CurrencyCode.PEN,
                CREATED_AT
        );

        assertThat(account.status())
                .isEqualTo(AccountStatus.ACTIVE);

        assertThat(account.balance())
                .isEqualTo(Money.zero(CurrencyCode.PEN));
    }

    @Test
    void shouldDepositFunds() {
        Account account = activeAccount("100.00");

        account.deposit(money("50.00"));

        assertThat(account.balance())
                .isEqualTo(money("150.00"));
    }

    @Test
    void shouldDebitFunds() {
        Account account = activeAccount("100.00");

        account.debit(money("40.00"));

        assertThat(account.balance())
                .isEqualTo(money("60.00"));
    }

    @Test
    void shouldCreditFunds() {
        Account account = activeAccount("100.00");

        account.credit(money("25.00"));

        assertThat(account.balance())
                .isEqualTo(money("125.00"));
    }

    @Test
    void shouldRejectDebitWhenBalanceIsInsufficient() {
        Account account = activeAccount("50.00");

        assertThatThrownBy(() ->
                account.debit(money("80.00"))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "resulting amount must not be negative"
                );
    }

    @Test
    void shouldRejectOperationsWhenAccountIsBlocked() {
        Account account = activeAccount("100.00");
        account.block();

        assertThatThrownBy(() ->
                account.debit(money("10.00"))
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("account must be active");
    }

    @Test
    void shouldActivateBlockedAccount() {
        Account account = activeAccount("100.00");

        account.block();
        account.activate();

        assertThat(account.status())
                .isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void shouldRejectClosingAccountWithRemainingBalance() {
        Account account = activeAccount("100.00");

        assertThatThrownBy(account::close)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "account with remaining balance cannot be closed"
                );
    }

    @Test
    void shouldCloseAccountWithZeroBalance() {
        Account account = activeAccount("0.00");

        account.close();

        assertThat(account.status())
                .isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void shouldRejectActivatingClosedAccount() {
        Account account = activeAccount("0.00");
        account.close();

        assertThatThrownBy(account::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "closed account cannot be activated"
                );
    }

    @Test
    void shouldRejectDifferentBalanceCurrency() {
        assertThatThrownBy(() ->
                new Account(
                        AccountId.generate(),
                        "001-1234567890",
                        CurrencyCode.PEN,
                        new Money(
                                new BigDecimal("100.00"),
                                CurrencyCode.USD
                        ),
                        AccountStatus.ACTIVE,
                        CREATED_AT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "account and balance currencies must match"
                );
    }

    @Test
    void shouldRejectZeroDeposit() {
        Account account = activeAccount("100.00");

        assertThatThrownBy(() ->
                account.deposit(Money.zero(CurrencyCode.PEN))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "amount must be greater than zero"
                );
    }

    private Account activeAccount(String balance) {
        return new Account(
                AccountId.generate(),
                "001-1234567890",
                CurrencyCode.PEN,
                money(balance),
                AccountStatus.ACTIVE,
                CREATED_AT
        );
    }

    private Money money(String amount) {
        return new Money(
                new BigDecimal(amount),
                CurrencyCode.PEN
        );
    }
}