package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.*;
import com.olek.banking.account.domain.exception.AccountNotActiveException;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.account.domain.exception.CurrencyMismatchException;
import com.olek.banking.account.domain.exception.InsufficientBalanceException;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.shared.application.transaction.TransactionExecutor;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferRepository;
import com.olek.banking.transfer.domain.exception.IdempotencyKeyConflictException;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Creates and processes internal money transfers between bank accounts.
 */
public final class CreateTransferService {

    private final AccountRepository accountRepository;
    private final AccountLockRepository accountLockRepository;
    private final TransferRepository transferRepository;
    private final AccountMovementRepository movementRepository;
    private final TransactionExecutor transactionExecutor;
    private final Clock clock;

    /**
     * Creates the transfer processing service.
     *
     * @param accountRepository   account persistence port
     * @param transferRepository  transfer persistence port
     * @param movementRepository  movement persistence port
     * @param transactionExecutor transaction boundary
     * @param clock               source of the current time
     */
    public CreateTransferService(
            AccountRepository accountRepository,
            AccountLockRepository accountLockRepository,
            TransferRepository transferRepository,
            AccountMovementRepository movementRepository,
            TransactionExecutor transactionExecutor,
            Clock clock
    ) {
        this.accountRepository = Objects.requireNonNull(
                accountRepository,
                "accountRepository must not be null"
        );

        this.accountLockRepository = Objects.requireNonNull(
                accountLockRepository,
                "accountLockRepository must not be null"
        );

        this.transferRepository = Objects.requireNonNull(
                transferRepository,
                "transferRepository must not be null"
        );

        this.movementRepository = Objects.requireNonNull(
                movementRepository,
                "movementRepository must not be null"
        );

        this.transactionExecutor = Objects.requireNonNull(
                transactionExecutor,
                "transactionExecutor must not be null"
        );

        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null"
        );
    }

    /**
     * Creates and processes an internal transfer atomically.
     *
     * @param command transfer information
     * @return created or previously processed transfer
     */
    public Transfer create(CreateTransferCommand command) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        TransferExecutionResult result =
                transactionExecutor.execute(
                        () -> executeTransfer(command)
                );

        if (result.wasRejected()) {
            throw result.rejection();
        }

        return result.transfer();
    }

    private TransferExecutionResult executeTransfer(
            CreateTransferCommand command
    ) {
        Transfer existingTransfer = transferRepository
                .findByIdempotencyKey(
                        command.idempotencyKey()
                )
                .orElse(null);

        if (existingTransfer != null) {
            return TransferExecutionResult.completed(
                    handleExistingTransfer(
                            existingTransfer,
                            command
                    )
            );
        }

        Instant createdAt = Instant.now(clock);

        Transfer transfer = Transfer.create(
                TransferId.generate(),
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount(),
                command.description(),
                command.idempotencyKey(),
                createdAt
        );

        transferRepository.save(transfer);

        try {
            return processTransfer(
                    transfer,
                    command
            );
        } catch (DomainException exception) {
            transfer.reject(
                    exception.code().name(),
                    Instant.now(clock)
            );

            Transfer rejectedTransfer =
                    transferRepository.save(transfer);

            return TransferExecutionResult.rejected(
                    rejectedTransfer,
                    exception
            );
        }
    }

    private TransferExecutionResult processTransfer(
            Transfer transfer,
            CreateTransferCommand command
    ) {
        LockedAccounts lockedAccounts =
                lockTransferAccounts(command);

        Account sourceAccount = lockedAccounts.source();
        Account destinationAccount =
                lockedAccounts.destination();

        validateTransfer(
                sourceAccount,
                destinationAccount,
                command
        );

        sourceAccount.debit(command.amount());
        destinationAccount.credit(command.amount());

        Account savedSource =
                accountRepository.save(sourceAccount);

        Account savedDestination =
                accountRepository.save(destinationAccount);

        Instant processedAt = Instant.now(clock);

        AccountMovement debitMovement = AccountMovement.debit(
                savedSource.id(),
                transfer.id(),
                command.amount(),
                savedSource.balance(),
                processedAt
        );

        AccountMovement creditMovement = AccountMovement.credit(
                savedDestination.id(),
                transfer.id(),
                command.amount(),
                savedDestination.balance(),
                processedAt
        );

        movementRepository.save(debitMovement);
        movementRepository.save(creditMovement);

        transfer.complete(processedAt);

        Transfer completedTransfer =
                transferRepository.save(transfer);

        return TransferExecutionResult.completed(
                completedTransfer
        );
    }

    private LockedAccounts lockTransferAccounts(
            CreateTransferCommand command
    ) {
        List<AccountId> orderedIds = Stream.of(
                        command.sourceAccountId(),
                        command.destinationAccountId()
                )
                .sorted(
                        Comparator.comparing(
                                AccountId::value
                        )
                )
                .toList();

        List<Account> lockedAccounts =
                accountLockRepository
                        .findAllByIdsForUpdate(orderedIds);

        if (lockedAccounts.size() != 2) {
            throwMissingAccount(
                    command,
                    lockedAccounts
            );
        }

        Account source = findAccountById(
                lockedAccounts,
                command.sourceAccountId()
        );

        Account destination = findAccountById(
                lockedAccounts,
                command.destinationAccountId()
        );

        return new LockedAccounts(source, destination);
    }

    private void throwMissingAccount(
            CreateTransferCommand command,
            List<Account> existingAccounts
    ) {
        boolean sourceExists = existingAccounts.stream()
                .anyMatch(account ->
                        account.id().equals(
                                command.sourceAccountId()
                        )
                );

        if (!sourceExists) {
            throw new AccountNotFoundException(
                    command.sourceAccountId()
            );
        }

        throw new AccountNotFoundException(
                command.destinationAccountId()
        );
    }

    private Account findAccountById(
            List<Account> accounts,
            AccountId accountId
    ) {
        return accounts.stream()
                .filter(account ->
                        account.id().equals(accountId)
                )
                .findFirst()
                .orElseThrow(
                        () -> new AccountNotFoundException(
                                accountId
                        )
                );
    }

    private Transfer handleExistingTransfer(
            Transfer existingTransfer,
            CreateTransferCommand command
    ) {
        if (!matches(existingTransfer, command)) {
            throw new IdempotencyKeyConflictException(
                    existingTransfer
            );
        }

        return existingTransfer;
    }

    private boolean matches(
            Transfer transfer,
            CreateTransferCommand command
    ) {
        return transfer.sourceAccountId()
                .equals(command.sourceAccountId())
                && transfer.destinationAccountId()
                .equals(command.destinationAccountId())
                && transfer.amount()
                .equals(command.amount())
                && transfer.description()
                .equals(command.description());
    }

    private Account findAccount(AccountId accountId) {
        return accountRepository
                .findById(accountId)
                .orElseThrow(
                        () -> new AccountNotFoundException(accountId)
                );
    }

    private void validateTransfer(
            Account sourceAccount,
            Account destinationAccount,
            CreateTransferCommand command
    ) {
        requireActive(sourceAccount);
        requireActive(destinationAccount);

        requireMatchingCurrency(
                sourceAccount,
                command.amount()
        );

        requireMatchingCurrency(
                destinationAccount,
                command.amount()
        );

        if (sourceAccount.balance()
                .isLessThan(command.amount())) {
            throw new InsufficientBalanceException(
                    sourceAccount.id(),
                    sourceAccount.balance(),
                    command.amount()
            );
        }
    }

    private void requireActive(Account account) {
        if (account.status() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    account.id(),
                    account.status()
            );
        }
    }

    private void requireMatchingCurrency(
            Account account,
            Money amount
    ) {
        if (account.currency() != amount.currency()) {
            throw new CurrencyMismatchException(
                    account.currency(),
                    amount.currency()
            );
        }
    }

    private record TransferExecutionResult(
            Transfer transfer,
            DomainException rejection
    ) {

        private static TransferExecutionResult completed(
                Transfer transfer
        ) {
            return new TransferExecutionResult(
                    transfer,
                    null
            );
        }

        private static TransferExecutionResult rejected(
                Transfer transfer,
                DomainException rejection
        ) {
            return new TransferExecutionResult(
                    transfer,
                    rejection
            );
        }

        private boolean wasRejected() {
            return rejection != null;
        }
    }

    private record LockedAccounts(
            Account source,
            Account destination
    ) {
    }
}