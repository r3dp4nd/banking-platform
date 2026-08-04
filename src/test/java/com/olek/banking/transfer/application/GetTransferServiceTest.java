package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferRepository;
import com.olek.banking.transfer.domain.exception.TransferNotFoundException;
import com.olek.banking.transfer.infrastructure.persistence.InMemoryTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetTransferServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T14:00:00Z");

    private TransferRepository transferRepository;
    private GetTransferService service;

    @BeforeEach
    void setUp() {
        transferRepository = new InMemoryTransferRepository();
        service = new GetTransferService(transferRepository);
    }

    @Test
    void shouldReturnExistingTransfer() {
        Transfer transfer = transfer();
        transferRepository.save(transfer);

        Transfer result = service.getById(transfer.id());

        assertThat(result).isSameAs(transfer);
    }

    @Test
    void shouldRejectMissingTransfer() {
        TransferId transferId = TransferId.generate();

        assertThatThrownBy(() ->
                service.getById(transferId)
        )
                .isInstanceOf(TransferNotFoundException.class)
                .hasMessage("transfer not found")
                .satisfies(exception -> {
                    TransferNotFoundException domainException =
                            (TransferNotFoundException) exception;

                    assertThat(domainException.code())
                            .isEqualTo(
                                    DomainErrorCode.TRANSFER_NOT_FOUND
                            );

                    assertThat(domainException.context())
                            .containsEntry(
                                    "transferId",
                                    transferId.toString()
                            );
                });
    }

    @Test
    void shouldRejectNullTransferId() {
        assertThatThrownBy(() -> service.getById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("transferId must not be null");
    }

    private Transfer transfer() {
        return Transfer.create(
                TransferId.generate(),
                AccountId.generate(),
                AccountId.generate(),
                new Money(
                        new BigDecimal("100.00"),
                        CurrencyCode.PEN
                ),
                "Payment for services",
                "transfer-request-001",
                CREATED_AT
        );
    }
}