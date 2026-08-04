package com.olek.banking.account.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountLockRepository;
import com.olek.banking.account.domain.AccountRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Persists account domain entities through Spring Data JPA.
 */
@Repository
@Profile("!memory")
public class JpaAccountRepository implements AccountRepository, AccountLockRepository {

    private final SpringDataAccountJpaRepository jpaRepository;

    /**
     * Creates the JPA account repository adapter.
     *
     * @param jpaRepository Spring Data account repository
     */
    public JpaAccountRepository(
            SpringDataAccountJpaRepository jpaRepository
    ) {
        this.jpaRepository = Objects.requireNonNull(
                jpaRepository,
                "jpaRepository must not be null"
        );
    }

    /**
     * Saves a domain account.
     *
     * @param account account to persist
     * @return persisted domain account
     */
    @Override
    public Account save(Account account) {
        Objects.requireNonNull(
                account,
                "account must not be null"
        );

        AccountJpaEntity savedEntity = jpaRepository.save(
                AccountPersistenceMapper.toEntity(account)
        );

        return AccountPersistenceMapper.toDomain(savedEntity);
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

        return jpaRepository
                .findById(accountId.value())
                .map(AccountPersistenceMapper::toDomain);
    }

    /**
     * Finds an account by its externally visible account number.
     *
     * @param accountNumber account number
     * @return account when found
     */
    @Override
    public Optional<Account> findByAccountNumber(
            String accountNumber
    ) {
        String normalized = normalizeAccountNumber(
                accountNumber
        );

        return jpaRepository
                .findByAccountNumber(normalized)
                .map(AccountPersistenceMapper::toDomain);
    }

    /**
     * Returns all persisted accounts.
     *
     * @return immutable account collection
     */
    @Override
    public List<Account> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(AccountPersistenceMapper::toDomain)
                .toList();
    }

    /**
     * Checks whether an account number is registered.
     *
     * @param accountNumber account number to verify
     * @return {@code true} when the account number exists
     */
    @Override
    public boolean existsByAccountNumber(
            String accountNumber
    ) {
        return jpaRepository.existsByAccountNumber(
                normalizeAccountNumber(accountNumber)
        );
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

        List<UUID> identifiers = accountIds.stream()
                .map(AccountId::value)
                .toList();

        return jpaRepository
                .findAllByIdForUpdate(identifiers)
                .stream()
                .map(AccountPersistenceMapper::toDomain)
                .toList();
    }
}