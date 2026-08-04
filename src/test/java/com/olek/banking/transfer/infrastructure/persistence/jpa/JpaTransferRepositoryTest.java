package com.olek.banking.transfer.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JpaTransferRepositoryTest {

    private static final UUID TRANSFER_ID =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T17:00:00Z");

    private SpringDataTransferJpaRepository springDataRepository;
    private JpaTransferRepository repository;

    @BeforeEach
    void setUp() {
        springDataRepository = mock(
                SpringDataTransferJpaRepository.class
        );

        repository = new JpaTransferRepository(
                springDataRepository
        );
    }

    @Test
    void shouldSaveDomainTransfer() {
        Transfer transfer = transfer();

        TransferJpaEntity persistedEntity =
                TransferPersistenceMapper.toEntity(transfer);

        when(
                springDataRepository.save(
                        any(TransferJpaEntity.class)
                )
        ).thenReturn(persistedEntity);

        Transfer result = repository.save(transfer);

        ArgumentCaptor<TransferJpaEntity> captor =
                ArgumentCaptor.forClass(
                        TransferJpaEntity.class
                );

        verify(springDataRepository).save(captor.capture());

        assertThat(captor.getValue().getId())
                .isEqualTo(TRANSFER_ID);

        assertThat(captor.getValue().getIdempotencyKey())
                .isEqualTo("transfer-request-001");

        assertThat(result.id())
                .isEqualTo(transfer.id());
    }

    @Test
    void shouldFindTransferById() {
        TransferJpaEntity entity =
                TransferPersistenceMapper.toEntity(transfer());

        when(springDataRepository.findById(TRANSFER_ID))
                .thenReturn(Optional.of(entity));

        assertThat(
                repository.findById(
                        new TransferId(TRANSFER_ID)
                )
        )
                .get()
                .extracting(Transfer::idempotencyKey)
                .isEqualTo("transfer-request-001");
    }

    @Test
    void shouldNormalizeIdempotencyKeyWhenSearching() {
        TransferJpaEntity entity =
                TransferPersistenceMapper.toEntity(transfer());

        when(
                springDataRepository.findByIdempotencyKey(
                        "transfer-request-001"
                )
        ).thenReturn(Optional.of(entity));

        assertThat(
                repository.findByIdempotencyKey(
                        " transfer-request-001 "
                )
        ).isPresent();

        verify(springDataRepository)
                .findByIdempotencyKey(
                        "transfer-request-001"
                );
    }

    @Test
    void shouldCheckExistingIdempotencyKey() {
        when(
                springDataRepository.existsByIdempotencyKey(
                        "transfer-request-001"
                )
        ).thenReturn(true);

        assertThat(
                repository.existsByIdempotencyKey(
                        " transfer-request-001 "
                )
        ).isTrue();
    }

    @Test
    void shouldReturnAllTransfers() {
        TransferJpaEntity entity =
                TransferPersistenceMapper.toEntity(transfer());

        when(springDataRepository.findAll())
                .thenReturn(List.of(entity));

        assertThat(repository.findAll())
                .singleElement()
                .extracting(Transfer::id)
                .isEqualTo(new TransferId(TRANSFER_ID));
    }

    private Transfer transfer() {
        return Transfer.create(
                new TransferId(TRANSFER_ID),
                AccountId.from(
                        "11111111-1111-1111-1111-111111111111"
                ),
                AccountId.from(
                        "22222222-2222-2222-2222-222222222222"
                ),
                new Money(
                        new BigDecimal("150.00"),
                        CurrencyCode.PEN
                ),
                "Payment for services",
                "transfer-request-001",
                CREATED_AT
        );
    }
}