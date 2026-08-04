package com.olek.banking.account.domain;

import java.util.List;
import java.util.Optional;

/**
 * Defines the persistence operations required by the account domain.
 *
 * <p>The domain declares this contract without depending on a specific
 * persistence technology.</p>
 */
public interface AccountRepository {

    /**
     * Saves a new account or updates an existing one.
     *
     * @param account account to persist
     * @return persisted account
     */
    Account save(Account account);

    /**
     * Finds an account by its identifier.
     *
     * @param accountId account identifier
     * @return account when found
     */
    Optional<Account> findById(AccountId accountId);

    /**
     * Finds an account by its externally visible account number.
     *
     * @param accountNumber account number
     * @return account when found
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Returns all persisted accounts.
     *
     * @return immutable account collection
     */
    List<Account> findAll();

    /**
     * Checks whether an account number is already registered.
     *
     * @param accountNumber account number to verify
     * @return {@code true} when the account number exists
     */
    boolean existsByAccountNumber(String accountNumber);
}