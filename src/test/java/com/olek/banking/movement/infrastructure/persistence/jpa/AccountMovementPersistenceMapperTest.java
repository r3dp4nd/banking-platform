package com.olek.banking.movement.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.MovementId;
import com.olek.banking.movement.domain.MovementType;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.TransferId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMovementPersistenceMapperTest {

    private static final UUID MOVEMENT_ID =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID TRANSFER_ID =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T17:30:00Z");

    @Test
    void shouldMapDebitMovementToJpaEntity() {
        AccountMovement movement = debitMovement();

        AccountMovementJpaEntity entity =
                AccountMovementPersistenceMapper.toEntity(
                        movement
                );

        assertThat(entity.getId())
                .isEqualTo(MOVEMENT_ID);

        assertThat(entity.getAccountId())
                .isEqualTo(ACCOUNT_ID);

        assertThat(entity.getTransferId())
                .isEqualTo(TRANSFER_ID);

        assertThat(entity.getMovementType())
                .isEqualTo(MovementType.DEBIT);

        assertThat(entity.getAmount())
                .isEqualByComparingTo("150.00");

        assertThat(entity.getBalanceAfter())
                .isEqualByComparingTo("350.00");

        assertThat(entity.getCurrency())
                .isEqualTo(CurrencyCode.PEN);

        assertThat(entity.getCreatedAt())
                .isEqualTo(CREATED_AT);
    }

    @Test
    void shouldMapDepositJpaEntityToDomain() {
        AccountMovementJpaEntity entity =
                new AccountMovementJpaEntity(
                        MOVEMENT_ID,
                        ACCOUNT_ID,
                        null,
                        MovementType.DEPOSIT,
                        new BigDecimal("500.00"),
                        CurrencyCode.PEN,
                        new BigDecimal("500.00"),
                        CREATED_AT
                );

        AccountMovement movement =
                AccountMovementPersistenceMapper.toDomain(
                        entity
                );

        assertThat(movement.id())
                .isEqualTo(new MovementId(MOVEMENT_ID));

        assertThat(movement.accountId())
                .isEqualTo(new AccountId(ACCOUNT_ID));

        assertThat(movement.transferId()).isNull();

        assertThat(movement.type())
                .isEqualTo(MovementType.DEPOSIT);

        assertThat(movement.amount())
                .isEqualTo(
                        new Money(
                                new BigDecimal("500.00"),
                                CurrencyCode.PEN
                        )
                );

        assertThat(movement.balanceAfter())
                .isEqualTo(
                        new Money(
                                new BigDecimal("500.00"),
                                CurrencyCode.PEN
                        )
                );
    }

    @Test
    void shouldPreserveMovementWhenMappingRoundTrip() {
        AccountMovement original = debitMovement();

        AccountMovement result =
                AccountMovementPersistenceMapper.toDomain(
                        AccountMovementPersistenceMapper.toEntity(
                                original
                        )
                );

        assertThat(result.id()).isEqualTo(original.id());
        assertThat(result.accountId())
                .isEqualTo(original.accountId());
        assertThat(result.transferId())
                .isEqualTo(original.transferId());
        assertThat(result.type()).isEqualTo(original.type());
        assertThat(result.amount()).isEqualTo(original.amount());
        assertThat(result.balanceAfter())
                .isEqualTo(original.balanceAfter());
        assertThat(result.createdAt())
                .isEqualTo(original.createdAt());
    }

    private AccountMovement debitMovement() {
        return new AccountMovement(
                new MovementId(MOVEMENT_ID),
                new AccountId(ACCOUNT_ID),
                new TransferId(TRANSFER_ID),
                MovementType.DEBIT,
                new Money(
                        new BigDecimal("150.00"),
                        CurrencyCode.PEN
                ),
                new Money(
                        new BigDecimal("350.00"),
                        CurrencyCode.PEN
                ),
                CREATED_AT
        );
    }
}