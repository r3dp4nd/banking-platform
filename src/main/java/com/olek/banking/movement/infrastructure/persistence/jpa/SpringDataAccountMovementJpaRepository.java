package com.olek.banking.movement.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Provides Spring Data JPA operations for movement persistence entities.
 */
public interface SpringDataAccountMovementJpaRepository
        extends JpaRepository<AccountMovementJpaEntity, UUID> {

    /**
     * Finds movements for an account in chronological order.
     *
     * @param accountId account identifier
     * @return ordered movement entities
     */
    List<AccountMovementJpaEntity>
    findByAccountIdOrderByCreatedAtAscIdAsc(UUID accountId);
}
