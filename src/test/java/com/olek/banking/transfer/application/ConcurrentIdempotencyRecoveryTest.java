package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountLockRepository;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.shared.infrastructure.transaction.DirectTransactionExecutor;
import com.olek.banking.transfer.application.exception.ConcurrentIdempotencyException;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConcurrentIdempotencyRecoveryTest {

    private static final Instant NOW =
            Instant.parse("2026-08-04T20:00:00Z");

    @Test
    void shouldReturnConcurrentWinningTransfer() {
        AccountRepository accountRepository =
                mock(AccountRepository.class);

        AccountLockRepository accountLockRepository =
                mock(AccountLockRepository.class);

        TransferRepository transferRepository =
                mock(TransferRepository.class);

        AccountMovementRepository movementRepository =
                mock(AccountMovementRepository.class);

        CreateTransferCommand command =
                command("concurrent-key-001");

        Transfer existingTransfer =
                completedTransfer(command);

        when(
                transferRepository.findByIdempotencyKey(
                        command.idempotencyKey()
                )
        )
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingTransfer));

        when(transferRepository.save(any(Transfer.class)))
                .thenThrow(
                        new ConcurrentIdempotencyException(
                                command.idempotencyKey(),
                                new RuntimeException(
                                        "simulated unique constraint"
                                )
                        )
                );

        CreateTransferService service =
                new CreateTransferService(
                        accountRepository,
                        accountLockRepository,
                        transferRepository,
                        movementRepository,
                        new DirectTransactionExecutor(),
                        Clock.fixed(NOW, ZoneOffset.UTC)
                );

        CreateTransferResult result =
                service.create(command);

        assertThat(result.transfer())
                .isSameAs(existingTransfer);

        assertThat(result.outcome())
                .isEqualTo(
                        CreateTransferResult.Outcome.RECOVERED
                );
    }

    private CreateTransferCommand command(
            String idempotencyKey
    ) {
        return new CreateTransferCommand(
                AccountId.from(
                        "11111111-1111-1111-1111-111111111111"
                ),
                AccountId.from(
                        "22222222-2222-2222-2222-222222222222"
                ),
                new Money(
                        new BigDecimal("80.00"),
                        CurrencyCode.PEN
                ),
                "Concurrent payment",
                idempotencyKey
        );
    }

    private Transfer completedTransfer(
            CreateTransferCommand command
    ) {
        Transfer transfer = Transfer.create(
                TransferId.from(
                        "33333333-3333-3333-3333-333333333333"
                ),
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount(),
                command.description(),
                command.idempotencyKey(),
                NOW
        );

        transfer.complete(NOW);
        return transfer;
    }
}