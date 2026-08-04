package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.*;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.account.domain.exception.InsufficientBalanceException;
import com.olek.banking.account.infrastructure.persistence.InMemoryAccountRepository;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.movement.domain.MovementType;
import com.olek.banking.movement.infrastructure.persistence.InMemoryAccountMovementRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.shared.infrastructure.transaction.DirectTransactionExecutor;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferRepository;
import com.olek.banking.transfer.domain.TransferStatus;
import com.olek.banking.transfer.domain.exception.IdempotencyKeyConflictException;
import com.olek.banking.transfer.infrastructure.persistence.InMemoryTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateTransferServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-04T14:00:00Z");

    private AccountRepository accountRepository;
    private AccountLockRepository accountLockRepository;
    private TransferRepository transferRepository;
    private AccountMovementRepository movementRepository;
    private CreateTransferService service;

    @BeforeEach
    void setUp() {
        InMemoryAccountRepository inMemoryAccountRepository =
                new InMemoryAccountRepository();

        accountRepository = inMemoryAccountRepository;
        accountLockRepository = inMemoryAccountRepository;
        transferRepository = new InMemoryTransferRepository();
        movementRepository =
                new InMemoryAccountMovementRepository();

        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new CreateTransferService(
                accountRepository,
                accountLockRepository,
                transferRepository,
                movementRepository,
                new DirectTransactionExecutor(),
                clock
        );
    }

    @Test
    void shouldCompleteTransferAndRecordMovements() {
        Account source = account(
                "001-1234567890",
                "500.00"
        );

        Account destination = account(
                "001-0987654321",
                "100.00"
        );

        accountRepository.save(source);
        accountRepository.save(destination);

        Transfer transfer = service.create(
                command(
                        source.id(),
                        destination.id(),
                        "150.00",
                        "transfer-request-001"
                )
        );

        assertThat(transfer.status())
                .isEqualTo(TransferStatus.COMPLETED);

        assertThat(source.balance())
                .isEqualTo(money("350.00"));

        assertThat(destination.balance())
                .isEqualTo(money("250.00"));

        assertThat(
                movementRepository.findByAccountId(source.id())
        )
                .singleElement()
                .satisfies(movement -> {
                    assertThat(movement.type())
                            .isEqualTo(MovementType.DEBIT);

                    assertThat(movement.transferId())
                            .isEqualTo(transfer.id());

                    assertThat(movement.amount())
                            .isEqualTo(money("150.00"));

                    assertThat(movement.balanceAfter())
                            .isEqualTo(money("350.00"));

                    assertThat(movement.createdAt())
                            .isEqualTo(NOW);
                });

        assertThat(
                movementRepository.findByAccountId(
                        destination.id()
                )
        )
                .singleElement()
                .satisfies(movement -> {
                    assertThat(movement.type())
                            .isEqualTo(MovementType.CREDIT);

                    assertThat(movement.transferId())
                            .isEqualTo(transfer.id());

                    assertThat(movement.amount())
                            .isEqualTo(money("150.00"));

                    assertThat(movement.balanceAfter())
                            .isEqualTo(money("250.00"));

                    assertThat(movement.createdAt())
                            .isEqualTo(NOW);
                });
    }

    @Test
    void shouldReturnExistingTransferForRepeatedRequest() {
        Account source = account(
                "001-1234567890",
                "500.00"
        );

        Account destination = account(
                "001-0987654321",
                "100.00"
        );

        accountRepository.save(source);
        accountRepository.save(destination);

        CreateTransferCommand command = command(
                source.id(),
                destination.id(),
                "150.00",
                "transfer-request-001"
        );

        Transfer firstResult = service.create(command);
        Transfer secondResult = service.create(command);

        assertThat(secondResult).isSameAs(firstResult);

        assertThat(source.balance())
                .isEqualTo(money("350.00"));

        assertThat(destination.balance())
                .isEqualTo(money("250.00"));

        assertThat(transferRepository.findAll())
                .containsExactly(firstResult);
    }

    @Test
    void shouldRejectReusedKeyWithDifferentAmount() {
        Account source = account(
                "001-1234567890",
                "500.00"
        );

        Account destination = account(
                "001-0987654321",
                "100.00"
        );

        accountRepository.save(source);
        accountRepository.save(destination);

        service.create(
                command(
                        source.id(),
                        destination.id(),
                        "100.00",
                        "transfer-request-001"
                )
        );

        assertThatThrownBy(() ->
                service.create(
                        command(
                                source.id(),
                                destination.id(),
                                "200.00",
                                "transfer-request-001"
                        )
                )
        )
                .isInstanceOf(
                        IdempotencyKeyConflictException.class
                )
                .hasMessage(
                        "idempotency key was already used "
                                + "with different data"
                );
    }

    @Test
    void shouldRejectTransferWithInsufficientBalance() {
        Account source = account(
                "001-1234567890",
                "50.00"
        );

        Account destination = account(
                "001-0987654321",
                "100.00"
        );

        accountRepository.save(source);
        accountRepository.save(destination);

        assertThatThrownBy(() ->
                service.create(
                        command(
                                source.id(),
                                destination.id(),
                                "150.00",
                                "transfer-request-001"
                        )
                )
        )
                .isInstanceOf(
                        InsufficientBalanceException.class
                );

        assertThat(source.balance())
                .isEqualTo(money("50.00"));

        assertThat(destination.balance())
                .isEqualTo(money("100.00"));

        assertThat(
                transferRepository.findByIdempotencyKey(
                        "transfer-request-001"
                )
        )
                .get()
                .extracting(Transfer::status)
                .isEqualTo(TransferStatus.REJECTED);
    }

    @Test
    void shouldRejectMissingSourceAccount() {
        AccountId missingSourceId = AccountId.generate();

        Account destination = account(
                "001-0987654321",
                "100.00"
        );

        accountRepository.save(destination);

        assertThatThrownBy(() ->
                service.create(
                        command(
                                missingSourceId,
                                destination.id(),
                                "50.00",
                                "transfer-request-001"
                        )
                )
        )
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("account not found");

        assertThat(destination.balance())
                .isEqualTo(money("100.00"));

        assertThat(
                transferRepository.findByIdempotencyKey(
                        "transfer-request-001"
                )
        )
                .get()
                .extracting(Transfer::status)
                .isEqualTo(TransferStatus.REJECTED);
    }

    @Test
    void shouldRejectBlockedDestinationBeforeDebitingSource() {
        Account source = account(
                "001-1234567890",
                "500.00"
        );

        Account destination = account(
                "001-0987654321",
                "100.00"
        );

        destination.block();

        accountRepository.save(source);
        accountRepository.save(destination);

        assertThatThrownBy(() ->
                service.create(
                        command(
                                source.id(),
                                destination.id(),
                                "150.00",
                                "transfer-request-001"
                        )
                )
        )
                .hasMessage("account must be active");

        assertThat(source.balance())
                .isEqualTo(money("500.00"));

        assertThat(destination.balance())
                .isEqualTo(money("100.00"));
    }

    @Test
    void shouldNotCreateDuplicateMovementsForRepeatedRequest() {
        Account source = account(
                "001-1234567890",
                "500.00"
        );

        Account destination = account(
                "001-0987654321",
                "100.00"
        );

        accountRepository.save(source);
        accountRepository.save(destination);

        CreateTransferCommand command = command(
                source.id(),
                destination.id(),
                "150.00",
                "transfer-request-001"
        );

        Transfer firstResult = service.create(command);
        Transfer secondResult = service.create(command);

        assertThat(secondResult).isSameAs(firstResult);

        assertThat(source.balance())
                .isEqualTo(money("350.00"));

        assertThat(destination.balance())
                .isEqualTo(money("250.00"));

        assertThat(movementRepository.findAll())
                .hasSize(2);

        assertThat(
                movementRepository.findByAccountId(source.id())
        )
                .singleElement()
                .extracting(AccountMovement::type)
                .isEqualTo(MovementType.DEBIT);

        assertThat(
                movementRepository.findByAccountId(
                        destination.id()
                )
        )
                .singleElement()
                .extracting(AccountMovement::type)
                .isEqualTo(MovementType.CREDIT);
    }

    @Test
    void shouldNotRecordMovementsWhenBalanceIsInsufficient() {
        Account source = account(
                "001-1234567890",
                "50.00"
        );

        Account destination = account(
                "001-0987654321",
                "100.00"
        );

        accountRepository.save(source);
        accountRepository.save(destination);

        assertThatThrownBy(() ->
                service.create(
                        command(
                                source.id(),
                                destination.id(),
                                "150.00",
                                "transfer-request-001"
                        )
                )
        )
                .isInstanceOf(
                        InsufficientBalanceException.class
                );

        assertThat(source.balance())
                .isEqualTo(money("50.00"));

        assertThat(destination.balance())
                .isEqualTo(money("100.00"));

        assertThat(movementRepository.findAll())
                .isEmpty();

        assertThat(
                transferRepository.findByIdempotencyKey(
                        "transfer-request-001"
                )
        )
                .get()
                .extracting(Transfer::status)
                .isEqualTo(TransferStatus.REJECTED);
    }

    private Account account(
            String accountNumber,
            String balance
    ) {
        return new Account(
                AccountId.generate(),
                accountNumber,
                CurrencyCode.PEN,
                money(balance),
                AccountStatus.ACTIVE,
                NOW
        );
    }

    private CreateTransferCommand command(
            AccountId sourceAccountId,
            AccountId destinationAccountId,
            String amount,
            String idempotencyKey
    ) {
        return new CreateTransferCommand(
                sourceAccountId,
                destinationAccountId,
                money(amount),
                "Payment for services",
                idempotencyKey
        );
    }

    private Money money(String amount) {
        return new Money(
                new BigDecimal(amount),
                CurrencyCode.PEN
        );
    }
}