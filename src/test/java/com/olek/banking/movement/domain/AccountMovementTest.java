package com.olek.banking.movement.domain;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.TransferId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountMovementTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T15:00:00Z");

    @Test
    void shouldCreateDepositMovement() {
        AccountId accountId = AccountId.generate();

        AccountMovement movement =
                AccountMovement.deposit(
                        accountId,
                        money("100.00"),
                        money("500.00"),
                        CREATED_AT
                );

        assertThat(movement.accountId())
                .isEqualTo(accountId);

        assertThat(movement.type())
                .isEqualTo(MovementType.DEPOSIT);

        assertThat(movement.transferId()).isNull();

        assertThat(movement.amount())
                .isEqualTo(money("100.00"));

        assertThat(movement.balanceAfter())
                .isEqualTo(money("500.00"));
    }

    @Test
    void shouldCreateDebitMovement() {
        TransferId transferId = TransferId.generate();

        AccountMovement movement =
                AccountMovement.debit(
                        AccountId.generate(),
                        transferId,
                        money("150.00"),
                        money("350.00"),
                        CREATED_AT
                );

        assertThat(movement.type())
                .isEqualTo(MovementType.DEBIT);

        assertThat(movement.transferId())
                .isEqualTo(transferId);
    }

    @Test
    void shouldCreateCreditMovement() {
        TransferId transferId = TransferId.generate();

        AccountMovement movement =
                AccountMovement.credit(
                        AccountId.generate(),
                        transferId,
                        money("150.00"),
                        money("250.00"),
                        CREATED_AT
                );

        assertThat(movement.type())
                .isEqualTo(MovementType.CREDIT);

        assertThat(movement.transferId())
                .isEqualTo(transferId);
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThatThrownBy(() ->
                AccountMovement.deposit(
                        AccountId.generate(),
                        Money.zero(CurrencyCode.PEN),
                        money("100.00"),
                        CREATED_AT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "movement amount must be greater than zero"
                );
    }

    @Test
    void shouldRejectDifferentCurrencies() {
        assertThatThrownBy(() ->
                AccountMovement.deposit(
                        AccountId.generate(),
                        money("100.00", CurrencyCode.PEN),
                        money("100.00", CurrencyCode.USD),
                        CREATED_AT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "movement and balance currencies must match"
                );
    }

    @Test
    void shouldRejectDepositWithTransferReference() {
        assertThatThrownBy(() ->
                new AccountMovement(
                        MovementId.generate(),
                        AccountId.generate(),
                        TransferId.generate(),
                        MovementType.DEPOSIT,
                        money("100.00"),
                        money("100.00"),
                        CREATED_AT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "deposit movement cannot reference a transfer"
                );
    }

    @Test
    void shouldRejectDebitWithoutTransferReference() {
        assertThatThrownBy(() ->
                new AccountMovement(
                        MovementId.generate(),
                        AccountId.generate(),
                        null,
                        MovementType.DEBIT,
                        money("100.00"),
                        money("100.00"),
                        CREATED_AT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "transfer movement must reference a transfer"
                );
    }

    private Money money(String amount) {
        return money(amount, CurrencyCode.PEN);
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