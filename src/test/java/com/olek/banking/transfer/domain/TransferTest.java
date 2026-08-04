package com.olek.banking.transfer.domain;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T13:00:00Z");

    private static final Instant COMPLETED_AT =
            Instant.parse("2026-08-04T13:01:00Z");

    @Test
    void shouldCreatePendingTransfer() {
        Transfer transfer = pendingTransfer();

        assertThat(transfer.status())
                .isEqualTo(TransferStatus.PENDING);

        assertThat(transfer.completedAt()).isNull();
        assertThat(transfer.failureReason()).isNull();

        assertThat(transfer.amount())
                .isEqualTo(money("100.00"));
    }

    @Test
    void shouldNormalizeDescriptionAndIdempotencyKey() {
        Transfer transfer = Transfer.create(
                TransferId.generate(),
                AccountId.generate(),
                AccountId.generate(),
                money("100.00"),
                " Payment for services ",
                " transfer-request-001 ",
                CREATED_AT
        );

        assertThat(transfer.description())
                .isEqualTo("Payment for services");

        assertThat(transfer.idempotencyKey())
                .isEqualTo("transfer-request-001");
    }

    @Test
    void shouldUseEmptyDescriptionWhenNull() {
        Transfer transfer = Transfer.create(
                TransferId.generate(),
                AccountId.generate(),
                AccountId.generate(),
                money("100.00"),
                null,
                "transfer-request-001",
                CREATED_AT
        );

        assertThat(transfer.description()).isEmpty();
    }

    @Test
    void shouldCompletePendingTransfer() {
        Transfer transfer = pendingTransfer();

        transfer.complete(COMPLETED_AT);

        assertThat(transfer.status())
                .isEqualTo(TransferStatus.COMPLETED);

        assertThat(transfer.completedAt())
                .isEqualTo(COMPLETED_AT);

        assertThat(transfer.failureReason()).isNull();
    }

    @Test
    void shouldRejectPendingTransfer() {
        Transfer transfer = pendingTransfer();

        transfer.reject(
                "account has insufficient balance",
                COMPLETED_AT
        );

        assertThat(transfer.status())
                .isEqualTo(TransferStatus.REJECTED);

        assertThat(transfer.completedAt())
                .isEqualTo(COMPLETED_AT);

        assertThat(transfer.failureReason())
                .isEqualTo(
                        "account has insufficient balance"
                );
    }

    @Test
    void shouldFailPendingTransfer() {
        Transfer transfer = pendingTransfer();

        transfer.fail(
                "unexpected processing failure",
                COMPLETED_AT
        );

        assertThat(transfer.status())
                .isEqualTo(TransferStatus.FAILED);

        assertThat(transfer.failureReason())
                .isEqualTo(
                        "unexpected processing failure"
                );
    }

    @Test
    void shouldRejectTransferBetweenSameAccount() {
        AccountId accountId = AccountId.generate();

        assertThatThrownBy(() ->
                Transfer.create(
                        TransferId.generate(),
                        accountId,
                        accountId,
                        money("100.00"),
                        "Test",
                        "transfer-request-001",
                        CREATED_AT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "source and destination accounts must be different"
                );
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThatThrownBy(() ->
                Transfer.create(
                        TransferId.generate(),
                        AccountId.generate(),
                        AccountId.generate(),
                        Money.zero(CurrencyCode.PEN),
                        "Test",
                        "transfer-request-001",
                        CREATED_AT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "transfer amount must be greater than zero"
                );
    }

    @Test
    void shouldRejectBlankIdempotencyKey() {
        assertThatThrownBy(() ->
                Transfer.create(
                        TransferId.generate(),
                        AccountId.generate(),
                        AccountId.generate(),
                        money("100.00"),
                        "Test",
                        "   ",
                        CREATED_AT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "idempotencyKey must not be blank"
                );
    }

    @Test
    void shouldRejectSecondStatusTransition() {
        Transfer transfer = pendingTransfer();
        transfer.complete(COMPLETED_AT);

        assertThatThrownBy(() ->
                transfer.reject(
                        "another reason",
                        COMPLETED_AT
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "only pending transfers can change status"
                );
    }

    @Test
    void shouldRejectTerminalTransferWithoutCompletionDate() {
        assertThatThrownBy(() ->
                new Transfer(
                        TransferId.generate(),
                        AccountId.generate(),
                        AccountId.generate(),
                        money("100.00"),
                        "Test",
                        "transfer-request-001",
                        TransferStatus.COMPLETED,
                        CREATED_AT,
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "terminal transfer must have completedAt"
                );
    }

    @Test
    void shouldRejectFailedTransferWithoutReason() {
        assertThatThrownBy(() ->
                new Transfer(
                        TransferId.generate(),
                        AccountId.generate(),
                        AccountId.generate(),
                        money("100.00"),
                        "Test",
                        "transfer-request-001",
                        TransferStatus.FAILED,
                        CREATED_AT,
                        COMPLETED_AT,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "rejected or failed transfer must have a reason"
                );
    }

    private Transfer pendingTransfer() {
        return Transfer.create(
                TransferId.generate(),
                AccountId.generate(),
                AccountId.generate(),
                money("100.00"),
                "Payment for services",
                "transfer-request-001",
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