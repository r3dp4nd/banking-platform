package com.olek.banking.transfer.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.account.domain.exception.AccountNotActiveException;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.account.domain.exception.CurrencyMismatchException;
import com.olek.banking.account.domain.exception.InsufficientBalanceException;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferRepository;
import com.olek.banking.transfer.domain.exception.IdempotencyKeyConflictException;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Creates and processes internal money transfers between bank accounts.
 */
public final class CreateTransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final Clock clock;

    /**
     * Creates the transfer processing service.
     *
     * @param accountRepository  account persistence port
     * @param transferRepository transfer persistence port
     * @param clock              source of the current time
     */
    public CreateTransferService(
            AccountRepository accountRepository,
            TransferRepository transferRepository,
            Clock clock
    ) {
        this.accountRepository = Objects.requireNonNull(
                accountRepository,
                "accountRepository must not be null"
        );

        this.transferRepository = Objects.requireNonNull(
                transferRepository,
                "transferRepository must not be null"
        );

        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null"
        );
    }

    /**
     * Creates and processes an internal transfer.
     *
     * <p>When the idempotency key already belongs to an equivalent request,
     * the original transfer is returned without modifying account balances
     * again.</p>
     *
     * @param command transfer information
     * @return created or previously processed transfer
     * @throws NullPointerException            if the command is {@code null}
     * @throws IdempotencyKeyConflictException if the key was used with
     *                                         different transfer data
     * @throws DomainException                 if a business rule prevents processing
     */
    public Transfer create(CreateTransferCommand command) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        Transfer existingTransfer = transferRepository
                .findByIdempotencyKey(command.idempotencyKey())
                .orElse(null);

        if (existingTransfer != null) {
            return handleExistingTransfer(
                    existingTransfer,
                    command
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
            Account sourceAccount = findAccount(
                    command.sourceAccountId()
            );

            Account destinationAccount = findAccount(
                    command.destinationAccountId()
            );

            validateTransfer(
                    sourceAccount,
                    destinationAccount,
                    command
            );

            sourceAccount.debit(command.amount());
            destinationAccount.credit(command.amount());

            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            transfer.complete(Instant.now(clock));

            return transferRepository.save(transfer);
        } catch (DomainException exception) {
            transfer.reject(
                    exception.code().name(),
                    Instant.now(clock)
            );

            transferRepository.save(transfer);
            throw exception;
        }
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
}