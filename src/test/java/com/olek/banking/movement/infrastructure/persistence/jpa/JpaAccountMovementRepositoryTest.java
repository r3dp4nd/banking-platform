package com.olek.banking.movement.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.MovementId;
import com.olek.banking.movement.domain.MovementType;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JpaAccountMovementRepositoryTest {

    private static final UUID MOVEMENT_ID =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T17:30:00Z");

    private SpringDataAccountMovementJpaRepository springDataRepository;
    private JpaAccountMovementRepository repository;

    @BeforeEach
    void setUp() {
        springDataRepository = mock(
                SpringDataAccountMovementJpaRepository.class
        );

        repository = new JpaAccountMovementRepository(
                springDataRepository
        );
    }

    @Test
    void shouldInsertMovement() {
        AccountMovement movement = movement();

        AccountMovementJpaEntity entity =
                AccountMovementPersistenceMapper.toEntity(
                        movement
                );

        when(springDataRepository.existsById(MOVEMENT_ID))
                .thenReturn(false);

        when(
                springDataRepository.saveAndFlush(
                        any(AccountMovementJpaEntity.class)
                )
        ).thenReturn(entity);

        AccountMovement result = repository.save(movement);

        ArgumentCaptor<AccountMovementJpaEntity> captor =
                ArgumentCaptor.forClass(
                        AccountMovementJpaEntity.class
                );

        verify(springDataRepository)
                .saveAndFlush(captor.capture());

        assertThat(captor.getValue().getId())
                .isEqualTo(MOVEMENT_ID);

        assertThat(result.id())
                .isEqualTo(movement.id());
    }

    @Test
    void shouldRejectExistingMovement() {
        AccountMovement movement = movement();

        when(springDataRepository.existsById(MOVEMENT_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> repository.save(movement))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "account movement already exists"
                );
    }

    @Test
    void shouldFindMovementById() {
        AccountMovementJpaEntity entity =
                AccountMovementPersistenceMapper.toEntity(
                        movement()
                );

        when(springDataRepository.findById(MOVEMENT_ID))
                .thenReturn(Optional.of(entity));

        assertThat(
                repository.findById(
                        new MovementId(MOVEMENT_ID)
                )
        )
                .get()
                .extracting(AccountMovement::accountId)
                .isEqualTo(new AccountId(ACCOUNT_ID));
    }

    @Test
    void shouldFindMovementsInChronologicalOrder() {
        AccountMovementJpaEntity entity =
                AccountMovementPersistenceMapper.toEntity(
                        movement()
                );

        when(
                springDataRepository
                        .findByAccountIdOrderByCreatedAtAscIdAsc(
                                ACCOUNT_ID
                        )
        ).thenReturn(List.of(entity));

        assertThat(
                repository.findByAccountId(
                        new AccountId(ACCOUNT_ID)
                )
        )
                .singleElement()
                .extracting(AccountMovement::id)
                .isEqualTo(new MovementId(MOVEMENT_ID));
    }

    private AccountMovement movement() {
        return new AccountMovement(
                new MovementId(MOVEMENT_ID),
                new AccountId(ACCOUNT_ID),
                null,
                MovementType.DEPOSIT,
                new Money(
                        new BigDecimal("500.00"),
                        CurrencyCode.PEN
                ),
                new Money(
                        new BigDecimal("500.00"),
                        CurrencyCode.PEN
                ),
                CREATED_AT
        );
    }
}