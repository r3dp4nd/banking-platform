package com.olek.banking.transfer.infrastructure.persistence.jpa;

import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.transfer.domain.TransferStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents the relational persistence model of a money transfer.
 *
 * <p>This entity belongs exclusively to the infrastructure layer and does not
 * contain business behavior.</p>
 */
@Entity
@Table(name = "bank_transfer")
public class TransferJpaEntity {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "source_account_id",
            nullable = false,
            updatable = false
    )
    private UUID sourceAccountId;

    @Column(
            name = "destination_account_id",
            nullable = false,
            updatable = false
    )
    private UUID destinationAccountId;

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
            name = "description",
            nullable = false,
            length = 140,
            updatable = false
    )
    private String description;

    @Column(
            name = "idempotency_key",
            nullable = false,
            length = 128,
            unique = true,
            updatable = false
    )
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private TransferStatus status;

    @Column(
            name = "failure_reason",
            length = 100
    )
    private String failureReason;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Creates an empty entity required by JPA.
     */
    protected TransferJpaEntity() {
    }

    /**
     * Creates a transfer persistence entity.
     *
     * @param id                   transfer identifier
     * @param sourceAccountId      source account identifier
     * @param destinationAccountId destination account identifier
     * @param amount               transfer amount
     * @param currency             transfer currency
     * @param description          transfer description
     * @param idempotencyKey       transfer idempotency key
     * @param status               transfer status
     * @param failureReason        safe rejection or failure reason
     * @param createdAt            transfer creation timestamp
     * @param completedAt          transfer completion timestamp
     */
    public TransferJpaEntity(
            UUID id,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            CurrencyCode currency,
            String description,
            String idempotencyKey,
            TransferStatus status,
            String failureReason,
            Instant createdAt,
            Instant completedAt
    ) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    /**
     * Returns the transfer identifier.
     *
     * @return transfer identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the source account identifier.
     *
     * @return source account identifier
     */
    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    /**
     * Returns the destination account identifier.
     *
     * @return destination account identifier
     */
    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    /**
     * Returns the transfer amount.
     *
     * @return transfer amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Returns the transfer currency.
     *
     * @return transfer currency
     */
    public CurrencyCode getCurrency() {
        return currency;
    }

    /**
     * Returns the transfer description.
     *
     * @return transfer description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the idempotency key.
     *
     * @return idempotency key
     */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * Returns the transfer status.
     *
     * @return transfer status
     */
    public TransferStatus getStatus() {
        return status;
    }

    /**
     * Returns the rejection or failure reason.
     *
     * @return failure reason, or {@code null}
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the completion timestamp.
     *
     * @return completion timestamp, or {@code null}
     */
    public Instant getCompletedAt() {
        return completedAt;
    }
}