package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferRepository;
import com.olek.banking.transfer.domain.TransferStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class ConcurrentIdempotencyIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountMovementRepository movementRepository;

    @Autowired
    private CreateTransferService transferService;

    @Test
    void shouldProcessConcurrentIdenticalRequestOnlyOnce()
            throws Exception {

        Account source = saveAccount(
                "001-9200000001",
                "500.00"
        );

        Account destination = saveAccount(
                "001-9200000002",
                "0.00"
        );

        CreateTransferCommand command =
                new CreateTransferCommand(
                        source.id(),
                        destination.id(),
                        money("150.00"),
                        "Concurrent idempotency test",
                        "concurrent-idempotency-001"
                );

        CountDownLatch start = new CountDownLatch(1);

        Callable<Transfer> request = () -> {
            start.await();
            return transferService.create(command);
        };

        Transfer firstResult;
        Transfer secondResult;

        try (ExecutorService executor =
                     Executors.newFixedThreadPool(2)) {

            Future<Transfer> first =
                    executor.submit(request);

            Future<Transfer> second =
                    executor.submit(request);

            start.countDown();

            firstResult = first.get();
            secondResult = second.get();
        }

        assertThat(firstResult.id())
                .isEqualTo(secondResult.id());

        assertThat(firstResult.status())
                .isEqualTo(TransferStatus.COMPLETED);

        assertThat(secondResult.status())
                .isEqualTo(TransferStatus.COMPLETED);

        assertThat(
                transferRepository.findAll()
                        .stream()
                        .filter(transfer ->
                                transfer.idempotencyKey()
                                        .equals(
                                                "concurrent-idempotency-001"
                                        )
                        )
        ).hasSize(1);

        assertThat(movementRepository.findAll())
                .filteredOn(movement ->
                        firstResult.id().equals(
                                movement.transferId()
                        )
                )
                .hasSize(2);

        Account persistedSource = accountRepository
                .findById(source.id())
                .orElseThrow();

        Account persistedDestination = accountRepository
                .findById(destination.id())
                .orElseThrow();

        assertThat(persistedSource.balance())
                .isEqualTo(money("350.00"));

        assertThat(persistedDestination.balance())
                .isEqualTo(money("150.00"));
    }

    @Test
    void shouldRejectConcurrentRequestWithDifferentPayload()
            throws Exception {

        Account source = saveAccount(
                "001-9200000011",
                "500.00"
        );

        Account destination = saveAccount(
                "001-9200000012",
                "0.00"
        );

        String idempotencyKey =
                "concurrent-idempotency-conflict-001";

        CreateTransferCommand firstCommand =
                new CreateTransferCommand(
                        source.id(),
                        destination.id(),
                        money("100.00"),
                        "First payload",
                        idempotencyKey
                );

        CreateTransferCommand secondCommand =
                new CreateTransferCommand(
                        source.id(),
                        destination.id(),
                        money("200.00"),
                        "Second payload",
                        idempotencyKey
                );

        CountDownLatch start = new CountDownLatch(1);

        Callable<Object> firstRequest = () -> {
            start.await();
            return execute(firstCommand);
        };

        Callable<Object> secondRequest = () -> {
            start.await();
            return execute(secondCommand);
        };

        List<Object> results;

        try (ExecutorService executor =
                     Executors.newFixedThreadPool(2)) {

            Future<Object> first =
                    executor.submit(firstRequest);

            Future<Object> second =
                    executor.submit(secondRequest);

            start.countDown();

            results = List.of(
                    first.get(),
                    second.get()
            );
        }

        assertThat(results)
                .filteredOn(Transfer.class::isInstance)
                .hasSize(1);

        assertThat(results)
                .filteredOn(
                        com.olek.banking.transfer.domain.exception
                                .IdempotencyKeyConflictException.class
                                ::isInstance
                )
                .hasSize(1);

        assertThat(
                transferRepository.findAll()
                        .stream()
                        .filter(transfer ->
                                transfer.idempotencyKey()
                                        .equals(idempotencyKey)
                        )
        ).hasSize(1);
    }

    private Account saveAccount(
            String accountNumber,
            String balance
    ) {
        return accountRepository.save(
                new Account(
                        AccountId.generate(),
                        accountNumber,
                        CurrencyCode.PEN,
                        money(balance),
                        AccountStatus.ACTIVE,
                        Instant.parse(
                                "2026-08-04T20:00:00Z"
                        )
                )
        );
    }

    private Object execute(
            CreateTransferCommand command
    ) {
        try {
            return transferService.create(command);
        } catch (
                com.olek.banking.transfer.domain.exception
                        .IdempotencyKeyConflictException exception
        ) {
            return exception;
        }
    }

    private Money money(String amount) {
        return new Money(
                new BigDecimal(amount),
                CurrencyCode.PEN
        );
    }
}