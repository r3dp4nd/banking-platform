package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.account.domain.exception.InsufficientBalanceException;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
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
class ConcurrentTransferIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CreateTransferService transferService;

    @Test
    void shouldPreventConcurrentDoubleSpending()
            throws Exception {

        Account source = saveAccount(
                "001-9100000001",
                "100.00"
        );

        Account destinationOne = saveAccount(
                "001-9100000002",
                "0.00"
        );

        Account destinationTwo = saveAccount(
                "001-9100000003",
                "0.00"
        );

        CountDownLatch start = new CountDownLatch(1);

        Callable<Object> firstTransfer = () -> {
            start.await();

            return executeTransfer(
                    source.id(),
                    destinationOne.id(),
                    "concurrent-transfer-001"
            );
        };

        Callable<Object> secondTransfer = () -> {
            start.await();

            return executeTransfer(
                    source.id(),
                    destinationTwo.id(),
                    "concurrent-transfer-002"
            );
        };

        try (ExecutorService executor =
                     Executors.newFixedThreadPool(2)) {

            Future<Object> first =
                    executor.submit(firstTransfer);

            Future<Object> second =
                    executor.submit(secondTransfer);

            start.countDown();

            List<Object> results = List.of(
                    first.get(),
                    second.get()
            );

            long completedCount = results.stream()
                    .filter(CreateTransferResult.class::isInstance)
                    .map(CreateTransferResult.class::cast)
                    .map(CreateTransferResult::transfer)
                    .filter(transfer ->
                            transfer.status() == TransferStatus.COMPLETED
                    )
                    .count();

            long rejectedCount = results.stream()
                    .filter(InsufficientBalanceException.class::isInstance)
                    .count();

            assertThat(completedCount)
                    .isEqualTo(1);

            assertThat(rejectedCount)
                    .isEqualTo(1);
        }

        Account persistedSource = accountRepository
                .findById(source.id())
                .orElseThrow();

        Account persistedDestinationOne = accountRepository
                .findById(destinationOne.id())
                .orElseThrow();

        Account persistedDestinationTwo = accountRepository
                .findById(destinationTwo.id())
                .orElseThrow();

        assertThat(persistedSource.balance())
                .isEqualTo(money("20.00"));

        assertThat(
                persistedDestinationOne.balance()
                        .amount()
                        .add(
                                persistedDestinationTwo
                                        .balance()
                                        .amount()
                        )
        ).isEqualByComparingTo("80.00");
    }

    private Object executeTransfer(
            AccountId sourceAccountId,
            AccountId destinationAccountId,
            String idempotencyKey
    ) {
        try {
            return transferService.create(
                    new CreateTransferCommand(
                            sourceAccountId,
                            destinationAccountId,
                            money("80.00"),
                            "Concurrent transfer test",
                            idempotencyKey
                    )
            );
        } catch (InsufficientBalanceException exception) {
            return exception;
        }
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
                                "2026-08-04T18:00:00Z"
                        )
                )
        );
    }

    private Money money(String amount) {
        return new Money(
                new BigDecimal(amount),
                CurrencyCode.PEN
        );
    }
}