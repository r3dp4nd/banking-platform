package com.olek.banking.account.infrastructure.persistence.jpa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    /**
     * Loads account entities using a pessimistic write lock.
     *
     * @param accountIds identifiers ordered deterministically
     * @return existing locked entities ordered by identifier
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select account
            from AccountJpaEntity account
            where account.id in :accountIds
            order by account.id
            """)
    List<AccountJpaEntity> findAllByIdForUpdate(
            @Param("accountIds") List<UUID> accountIds
    );
}