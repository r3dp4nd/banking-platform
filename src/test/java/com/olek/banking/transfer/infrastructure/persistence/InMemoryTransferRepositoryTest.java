package com.olek.banking.transfer.infrastructure.persistence;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static com.olek.banking.transfer.domain.TransferId.generate;
import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTransferRepositoryTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T13:00:00Z");

    private TransferRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransferRepository();
    }

    @Test
    void shouldSaveAndFindTransferById() {
        Transfer transfer = transfer("transfer-request-001");

        repository.save(transfer);

        assertThat(repository.findById(transfer.id()))
                .contains(transfer);
    }

    @Test
    void shouldFindTransferByIdempotencyKey() {
        Transfer transfer = transfer("transfer-request-001");
        repository.save(transfer);

        assertThat(
                repository.findByIdempotencyKey(
                        "transfer-request-001"
                )
        ).contains(transfer);
    }

    @Test
    void shouldNormalizeIdempotencyKeyWhenSearching() {
        Transfer transfer = transfer("transfer-request-001");
        repository.save(transfer);

        assertThat(
                repository.findByIdempotencyKey(
                        " transfer-request-001 "
                )
        ).contains(transfer);
    }

    @Test
    void shouldReturnEmptyWhenTransferDoesNotExist() {
        assertThat(
                repository.findByIdempotencyKey(
                        "missing-request"
                )
        ).isEmpty();
    }

    @Test
    void shouldReportExistingIdempotencyKey() {
        repository.save(
                transfer("transfer-request-001")
        );

        assertThat(
                repository.existsByIdempotencyKey(
                        "transfer-request-001"
                )
        ).isTrue();
    }

    @Test
    void shouldReturnAllTransfers() {
        repository.save(
                transfer("transfer-request-001")
        );

        repository.save(
                transfer("transfer-request-002")
        );

        assertThat(repository.findAll())
                .hasSize(2);
    }

    @Test
    void shouldReplaceTransferWithSameIdentifier() {
        Transfer transfer = transfer("transfer-request-001");

        repository.save(transfer);
        repository.save(transfer);

        assertThat(repository.findAll())
                .containsExactly(transfer);
    }

    private Transfer transfer(String idempotencyKey) {
        return Transfer.create(
                generate(),
                AccountId.generate(),
                AccountId.generate(),
                new Money(
                        new BigDecimal("100.00"),
                        CurrencyCode.PEN
                ),
                "Payment for services",
                idempotencyKey,
                CREATED_AT
        );
    }
}