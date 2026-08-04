package com.olek.banking.account.domain;

import java.util.List;

/**
 * Defines the account locking operation required by financial use cases.
 */
public interface AccountLockRepository {

    /**
     * Loads and locks the requested accounts for update.
     *
     * <p>The returned accounts must follow the same order as the supplied
     * identifiers.</p>
     *
     * @param accountIds account identifiers in deterministic locking order
     * @return existing locked accounts
     */
    List<Account> findAllByIdsForUpdate(
            List<AccountId> accountIds
    );
}