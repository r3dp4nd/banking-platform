package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.shared.application.transaction.TransactionExecutor;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Deposits test funds into existing bank accounts.
 */
public final class DepositFundsService {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository movementRepository;
    private final TransactionExecutor transactionExecutor;
    private final Clock clock;

    /**
     * Creates the deposit funds service.
     *
     * @param accountRepository   account persistence port
     * @param movementRepository  account movement persistence port
     * @param transactionExecutor transaction boundary
     * @param clock               source of the current time
     */
    public DepositFundsService(
            AccountRepository accountRepository,
            AccountMovementRepository movementRepository,
            TransactionExecutor transactionExecutor,
            Clock clock
    ) {
        this.accountRepository = Objects.requireNonNull(
                accountRepository,
                "accountRepository must not be null"
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
     * Deposits funds and records the movement atomically.
     *
     * @param command deposit information
     * @return updated account
     */
    public Account deposit(DepositFundsCommand command) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        return transactionExecutor.execute(
                () -> executeDeposit(command)
        );
    }

    private Account executeDeposit(
            DepositFundsCommand command
    ) {
        Account account = accountRepository
                .findById(command.accountId())
                .orElseThrow(
                        () -> new AccountNotFoundException(
                                command.accountId()
                        )
                );

        account.deposit(command.amount());
        Account savedAccount =
                accountRepository.save(account);

        AccountMovement movement = AccountMovement.deposit(
                savedAccount.id(),
                command.amount(),
                savedAccount.balance(),
                Instant.now(clock)
        );

        movementRepository.save(movement);

        return savedAccount;
    }
}