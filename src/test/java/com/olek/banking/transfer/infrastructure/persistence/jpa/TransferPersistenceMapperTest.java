package com.olek.banking.transfer.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransferPersistenceMapperTest {

    private static final UUID TRANSFER_ID =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private static final UUID SOURCE_ACCOUNT_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID DESTINATION_ACCOUNT_ID =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T17:00:00Z");

    private static final Instant COMPLETED_AT =
            Instant.parse("2026-08-04T17:01:00Z");

    @Test
    void shouldMapCompletedTransferToJpaEntity() {
        Transfer transfer = completedTransfer();

        TransferJpaEntity entity =
                TransferPersistenceMapper.toEntity(transfer);

        assertThat(entity.getId())
                .isEqualTo(TRANSFER_ID);

        assertThat(entity.getSourceAccountId())
                .isEqualTo(SOURCE_ACCOUNT_ID);

        assertThat(entity.getDestinationAccountId())
                .isEqualTo(DESTINATION_ACCOUNT_ID);

        assertThat(entity.getAmount())
                .isEqualByComparingTo("150.00");

        assertThat(entity.getCurrency())
                .isEqualTo(CurrencyCode.PEN);

        assertThat(entity.getIdempotencyKey())
                .isEqualTo("transfer-request-001");

        assertThat(entity.getStatus())
                .isEqualTo(TransferStatus.COMPLETED);

        assertThat(entity.getCompletedAt())
                .isEqualTo(COMPLETED_AT);

        assertThat(entity.getFailureReason())
                .isNull();
    }

    @Test
    void shouldMapPendingJpaEntityToDomain() {
        TransferJpaEntity entity = new TransferJpaEntity(
                TRANSFER_ID,
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                new BigDecimal("150.00"),
                CurrencyCode.PEN,
                "Payment for services",
                "transfer-request-001",
                TransferStatus.PENDING,
                null,
                CREATED_AT,
                null
        );

        Transfer transfer =
                TransferPersistenceMapper.toDomain(entity);

        assertThat(transfer.id())
                .isEqualTo(new TransferId(TRANSFER_ID));

        assertThat(transfer.sourceAccountId())
                .isEqualTo(new AccountId(SOURCE_ACCOUNT_ID));

        assertThat(transfer.destinationAccountId())
                .isEqualTo(new AccountId(DESTINATION_ACCOUNT_ID));

        assertThat(transfer.amount())
                .isEqualTo(
                        new Money(
                                new BigDecimal("150.00"),
                                CurrencyCode.PEN
                        )
                );

        assertThat(transfer.status())
                .isEqualTo(TransferStatus.PENDING);

        assertThat(transfer.completedAt()).isNull();
        assertThat(transfer.failureReason()).isNull();
    }

    @Test
    void shouldMapRejectedJpaEntityToDomain() {
        TransferJpaEntity entity = new TransferJpaEntity(
                TRANSFER_ID,
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                new BigDecimal("150.00"),
                CurrencyCode.PEN,
                "Payment for services",
                "transfer-request-001",
                TransferStatus.REJECTED,
                "INSUFFICIENT_BALANCE",
                CREATED_AT,
                COMPLETED_AT
        );

        Transfer transfer =
                TransferPersistenceMapper.toDomain(entity);

        assertThat(transfer.status())
                .isEqualTo(TransferStatus.REJECTED);

        assertThat(transfer.completedAt())
                .isEqualTo(COMPLETED_AT);

        assertThat(transfer.failureReason())
                .isEqualTo("INSUFFICIENT_BALANCE");
    }

    @Test
    void shouldPreserveTransferWhenMappingRoundTrip() {
        Transfer original = completedTransfer();

        Transfer result = TransferPersistenceMapper.toDomain(
                TransferPersistenceMapper.toEntity(original)
        );

        assertThat(result.id())
                .isEqualTo(original.id());

        assertThat(result.sourceAccountId())
                .isEqualTo(original.sourceAccountId());

        assertThat(result.destinationAccountId())
                .isEqualTo(original.destinationAccountId());

        assertThat(result.amount())
                .isEqualTo(original.amount());

        assertThat(result.description())
                .isEqualTo(original.description());

        assertThat(result.idempotencyKey())
                .isEqualTo(original.idempotencyKey());

        assertThat(result.status())
                .isEqualTo(original.status());

        assertThat(result.createdAt())
                .isEqualTo(original.createdAt());

        assertThat(result.completedAt())
                .isEqualTo(original.completedAt());
    }

    private Transfer completedTransfer() {
        Transfer transfer = Transfer.create(
                new TransferId(TRANSFER_ID),
                new AccountId(SOURCE_ACCOUNT_ID),
                new AccountId(DESTINATION_ACCOUNT_ID),
                new Money(
                        new BigDecimal("150.00"),
                        CurrencyCode.PEN
                ),
                "Payment for services",
                "transfer-request-001",
                CREATED_AT
        );

        transfer.complete(COMPLETED_AT);
        return transfer;
    }
}