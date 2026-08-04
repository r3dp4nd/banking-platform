package com.olek.banking.account.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides Spring Data JPA operations for account persistence entities.
 */
interface SpringDataAccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    /**
     * Finds an account entity by its externally visible account number.
     *
     * @param accountNumber account number
     * @return account entity when found
     */
    Optional<AccountJpaEntity> findByAccountNumber(
            String accountNumber
    );

    /**
     * Checks whether an account entity uses the given account number.
     *
     * @param accountNumber account number
     * @return {@code true} when the account number exists
     */
    boolean existsByAccountNumber(String accountNumber);
}