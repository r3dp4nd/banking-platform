package com.olek.banking.movement.infrastructure.persistence;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryAccountMovementRepositoryTest {

    private AccountMovementRepository repository;

    @BeforeEach
    void setUp() {
        repository =
                new InMemoryAccountMovementRepository();
    }

    @Test
    void shouldSaveAndFindMovementById() {
        AccountMovement movement = movement(
                AccountId.generate(),
                Instant.parse("2026-08-04T15:00:00Z")
        );

        repository.save(movement);

        assertThat(repository.findById(movement.id()))
                .contains(movement);
    }

    @Test
    void shouldFindMovementsByAccountInChronologicalOrder() {
        AccountId accountId = AccountId.generate();

        AccountMovement second = movement(
                accountId,
                Instant.parse("2026-08-04T15:02:00Z")
        );

        AccountMovement first = movement(
                accountId,
                Instant.parse("2026-08-04T15:01:00Z")
        );

        repository.save(second);
        repository.save(first);

        assertThat(repository.findByAccountId(accountId))
                .containsExactly(first, second);
    }

    @Test
    void shouldExcludeMovementsFromOtherAccounts() {
        AccountId requestedAccountId =
                AccountId.generate();

        AccountMovement expected = movement(
                requestedAccountId,
                Instant.parse("2026-08-04T15:00:00Z")
        );

        repository.save(expected);

        repository.save(
                movement(
                        AccountId.generate(),
                        Instant.parse(
                                "2026-08-04T15:01:00Z"
                        )
                )
        );

        assertThat(
                repository.findByAccountId(
                        requestedAccountId
                )
        ).containsExactly(expected);
    }

    @Test
    void shouldRejectSavingSameMovementTwice() {
        AccountMovement movement = movement(
                AccountId.generate(),
                Instant.parse("2026-08-04T15:00:00Z")
        );

        repository.save(movement);

        assertThatThrownBy(() ->
                repository.save(movement)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "account movement already exists"
                );
    }

    private AccountMovement movement(
            AccountId accountId,
            Instant createdAt
    ) {
        return AccountMovement.deposit(
                accountId,
                new Money(
                        new BigDecimal("100.00"),
                        CurrencyCode.PEN
                ),
                new Money(
                        new BigDecimal("500.00"),
                        CurrencyCode.PEN
                ),
                createdAt
        );
    }
}