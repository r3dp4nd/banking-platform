package com.olek.banking.shared.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldNormalizeAmountToTwoDecimalPlaces() {
        Money money = new Money(
                new BigDecimal("100"),
                CurrencyCode.PEN
        );

        assertThat(money.amount())
                .isEqualByComparingTo("100.00");
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money first = new Money(
                new BigDecimal("100.00"),
                CurrencyCode.PEN
        );

        Money second = new Money(
                new BigDecimal("50.00"),
                CurrencyCode.PEN
        );

        Money result = first.add(second);

        assertThat(result)
                .isEqualTo(new Money(
                        new BigDecimal("150.00"),
                        CurrencyCode.PEN
                ));
    }

    @Test
    void shouldSubtractMoneyWithSameCurrency() {
        Money balance = new Money(
                new BigDecimal("100.00"),
                CurrencyCode.PEN
        );

        Money withdrawal = new Money(
                new BigDecimal("40.00"),
                CurrencyCode.PEN
        );

        Money result = balance.subtract(withdrawal);

        assertThat(result)
                .isEqualTo(new Money(
                        new BigDecimal("60.00"),
                        CurrencyCode.PEN
                ));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() ->
                new Money(
                        new BigDecimal("-1.00"),
                        CurrencyCode.PEN
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must not be negative");
    }

    @Test
    void shouldRejectMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() ->
                new Money(
                        new BigDecimal("10.123"),
                        CurrencyCode.PEN
                )
        )
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void shouldRejectOperationsWithDifferentCurrencies() {
        Money soles = new Money(
                new BigDecimal("100.00"),
                CurrencyCode.PEN
        );

        Money dollars = new Money(
                new BigDecimal("20.00"),
                CurrencyCode.USD
        );

        assertThatThrownBy(() -> soles.add(dollars))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("currencies must match");
    }

    @Test
    void shouldRejectSubtractionWhenBalanceIsInsufficient() {
        Money balance = new Money(
                new BigDecimal("50.00"),
                CurrencyCode.PEN
        );

        Money withdrawal = new Money(
                new BigDecimal("80.00"),
                CurrencyCode.PEN
        );

        assertThatThrownBy(() -> balance.subtract(withdrawal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resulting amount must not be negative");
    }

    @Test
    void shouldCreateZeroMoney() {
        Money result = Money.zero(CurrencyCode.USD);

        assertThat(result)
                .isEqualTo(new Money(
                        new BigDecimal("0.00"),
                        CurrencyCode.USD
                ));

        assertThat(result.isZero()).isTrue();
    }
}
