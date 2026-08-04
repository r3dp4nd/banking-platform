package com.olek.banking.movement.infrastructure.persistence.jpa;

import com.olek.banking.movement.domain.MovementType;
import com.olek.banking.shared.domain.CurrencyCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents the relational persistence model of an account movement.
 *
 * <p>This entity is append-only and belongs exclusively to the
 * infrastructure layer.</p>
 */
@Entity
@Table(name = "account_movement")
public class AccountMovementJpaEntity {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "account_id",
            nullable = false,
            updatable = false
    )
    private UUID accountId;

    @Column(
            name = "transfer_id",
            updatable = false
    )
    private UUID transferId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "movement_type",
            nullable = false,
            length = 20,
            updatable = false
    )
    private MovementType movementType;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 2,
            updatable = false
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "currency",
            nullable = false,
            length = 3,
            updatable = false
    )
    private CurrencyCode currency;

    @Column(
            name = "balance_after",
            nullable = false,
            precision = 19,
            scale = 2,
            updatable = false
    )
    private BigDecimal balanceAfter;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    /**
     * Creates an empty entity required by JPA.
     */
    protected AccountMovementJpaEntity() {
    }

    /**
     * Creates an account movement persistence entity.
     *
     * @param id           movement identifier
     * @param accountId    affected account identifier
     * @param transferId   related transfer identifier, when available
     * @param movementType movement type
     * @param amount       movement amount
     * @param currency     movement currency
     * @param balanceAfter resulting account balance
     * @param createdAt    movement creation timestamp
     */
    public AccountMovementJpaEntity(
            UUID id,
            UUID accountId,
            UUID transferId,
            MovementType movementType,
            BigDecimal amount,
            CurrencyCode currency,
            BigDecimal balanceAfter,
            Instant createdAt
    ) {
        this.id = id;
        this.accountId = accountId;
        this.transferId = transferId;
        this.movementType = movementType;
        this.amount = amount;
        this.currency = currency;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}