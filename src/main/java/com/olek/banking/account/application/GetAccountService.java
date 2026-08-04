package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.exception.AccountNotFoundException;

import java.util.Objects;

/**
 * Retrieves bank accounts from the account repository.
 */
public final class GetAccountService {

    private final AccountRepository accountRepository;

    /**
     * Creates the account query service.
     *
     * @param accountRepository account persistence port
     */
    public GetAccountService(
            AccountRepository accountRepository
    ) {
        this.accountRepository = Objects.requireNonNull(
                accountRepository,
                "accountRepository must not be null"
        );
    }

    /**
     * Retrieves an account by its identifier.
     *
     * @param accountId account identifier
     * @return existing account
     * @throws NullPointerException     if the identifier is {@code null}
     * @throws AccountNotFoundException if the account does not exist
     */
    public Account getById(AccountId accountId) {
        Objects.requireNonNull(
                accountId,
                "accountId must not be null"
        );

        return accountRepository
                .findById(accountId)
                .orElseThrow(
                        () -> new AccountNotFoundException(accountId)
                );
    }
}