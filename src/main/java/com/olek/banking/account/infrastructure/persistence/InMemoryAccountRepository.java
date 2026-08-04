package com.olek.banking.account.infrastructure.persistence;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountLockRepository;
import com.olek.banking.account.domain.AccountRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores accounts in memory.
 *
 * <p>This adapter is intended for learning, local development and isolated
 * tests. Its contents are lost when the application stops.</p>
 */
public class InMemoryAccountRepository implements AccountRepository, AccountLockRepository {


    private final Map<AccountId, Account> accounts =
            new ConcurrentHashMap<>();

    /**
     * Saves a new account or replaces an existing account with the same ID.
     *
     * @param account account to persist
     * @return persisted account
     */
    @Override
    public Account save(Account account) {
        Objects.requireNonNull(
                account,
                "account must not be null"
        );

        accounts.put(account.id(), account);
        return account;
    }

    /**
     * Finds an account by its identifier.
     *
     * @param accountId account identifier
     * @return account when found
     */
    @Override
    public Optional<Account> findById(AccountId accountId) {
        Objects.requireNonNull(
                accountId,
                "accountId must not be null"
        );

        return Optional.ofNullable(accounts.get(accountId));
    }

    /**
     * Finds an account by its account number.
     *
     * @param accountNumber account number
     * @return account when found
     */
    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        String normalized = normalizeAccountNumber(accountNumber);

        return accounts.values()
                .stream()
                .filter(account ->
                        account.accountNumber().equals(normalized)
                )
                .findFirst();
    }

    /**
     * Returns all stored accounts.
     *
     * @return immutable account collection
     */
    @Override
    public List<Account> findAll() {
        return List.copyOf(accounts.values());
    }

    /**
     * Checks whether an account number already exists.
     *
     * @param accountNumber account number to verify
     * @return {@code true} when the account number exists
     */
    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return findByAccountNumber(accountNumber).isPresent();
    }

    private String normalizeAccountNumber(String value) {
        Objects.requireNonNull(
                value,
                "accountNumber must not be null"
        );

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "accountNumber must not be blank"
            );
        }

        return normalized;
    }

    @Override
    public List<Account> findAllByIdsForUpdate(List<AccountId> accountIds) {
        Objects.requireNonNull(
                accountIds,
                "accountIds must not be null"
        );

        return accountIds.stream()
                .map(accounts::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
