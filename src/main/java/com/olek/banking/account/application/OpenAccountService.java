package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.exception.AccountNumberAlreadyExistsException;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Opens bank accounts while coordinating domain creation and persistence.
 */
public class OpenAccountService {

    private final AccountRepository accountRepository;
    private final Clock clock;

    /**
     * Creates the account opening service.
     *
     * @param accountRepository account persistence port
     * @param clock             source of the current time
     */
    public OpenAccountService(
            AccountRepository accountRepository,
            Clock clock
    ) {
        this.accountRepository = Objects.requireNonNull(
                accountRepository,
                "accountRepository must not be null"
        );

        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null"
        );
    }

    /**
     * Opens and persists a new bank account.
     *
     * @param command account opening information
     * @return newly opened account
     * @throws NullPointerException                if the command is {@code null}
     * @throws AccountNumberAlreadyExistsException if the account number
     *                                             already exists
     */
    public Account open(OpenAccountCommand command) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        if (accountRepository.existsByAccountNumber(
                command.accountNumber()
        )) {
            throw new AccountNumberAlreadyExistsException(
                    command.accountNumber()
            );
        }

        Account account = Account.open(
                AccountId.generate(),
                command.accountNumber(),
                command.currency(),
                Instant.now(clock)
        );

        return accountRepository.save(account);
    }
}
