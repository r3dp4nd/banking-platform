package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Deposits test funds into existing bank accounts.
 */
public final class DepositFundsService {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository movementRepository;
    private final Clock clock;

    /**
     * Creates the deposit funds service.
     *
     * @param accountRepository account persistence port
     */
    public DepositFundsService(
            AccountRepository accountRepository,
            AccountMovementRepository movementRepository,
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

        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null"
        );
    }

    /**
     * Deposits funds into an existing account and persists its new balance.
     *
     * @param command deposit information
     * @return updated account
     * @throws NullPointerException     if the command is {@code null}
     * @throws AccountNotFoundException if the account does not exist
     */
    public Account deposit(DepositFundsCommand command) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        Account account = accountRepository
                .findById(command.accountId())
                .orElseThrow(
                        () -> new AccountNotFoundException(
                                command.accountId()
                        )
                );

        account.deposit(command.amount());
        accountRepository.save(account);

        AccountMovement movement = AccountMovement.deposit(
                account.id(),
                command.amount(),
                account.balance(),
                Instant.now(clock)
        );

        movementRepository.save(movement);

        return account;
    }
}