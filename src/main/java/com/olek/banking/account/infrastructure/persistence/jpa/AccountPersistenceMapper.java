package com.olek.banking.account.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.Money;

/**
 * Maps account domain models to and from JPA persistence entities.
 */
final class AccountPersistenceMapper {

    private AccountPersistenceMapper() {
    }

    /**
     * Maps an account domain entity to its JPA representation.
     *
     * @param account domain account
     * @return account persistence entity
     */
    static AccountJpaEntity toEntity(Account account) {
        return new AccountJpaEntity(
                account.id().value(),
                account.accountNumber(),
                account.currency(),
                account.balance().amount(),
                account.status(),
                account.createdAt()
        );
    }

    /**
     * Maps an account JPA entity to its domain representation.
     *
     * @param entity account persistence entity
     * @return reconstructed domain account
     */
    static Account toDomain(AccountJpaEntity entity) {
        return new Account(
                new AccountId(entity.getId()),
                entity.getAccountNumber(),
                entity.getCurrency(),
                new Money(
                        entity.getBalance(),
                        entity.getCurrency()
                ),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}