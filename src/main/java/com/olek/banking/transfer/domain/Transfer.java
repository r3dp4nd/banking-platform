package com.olek.banking.transfer.domain;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.Money;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a request to transfer money between two bank accounts.
 *
 * <p>A transfer starts in {@link TransferStatus#PENDING} and can transition
 * to a terminal status through explicit domain operations.</p>
 */
public final class Transfer {

    private final TransferId id;
    private final AccountId sourceAccountId;
    private final AccountId destinationAccountId;
    private final Money amount;
    private final String description;
    private final String idempotencyKey;
    private final Instant createdAt;

    private TransferStatus status;
    private Instant completedAt;
    private String failureReason;

    /**
     * Creates a transfer.
     *
     * @param id                   unique transfer identifier
     * @param sourceAccountId      account sending the funds
     * @param destinationAccountId account receiving the funds
     * @param amount               monetary amount to transfer
     * @param description          transfer description
     * @param idempotencyKey       key used to prevent duplicated processing
     * @param status               initial transfer status
     * @param createdAt            transfer creation timestamp
     * @param completedAt          transfer completion timestamp, when applicable
     * @param failureReason        safe reason associated with a rejected or failed
     *                             transfer
     * @throws NullPointerException     if a required value is {@code null}
     * @throws IllegalArgumentException if accounts are equal, the amount is
     *                                  zero, or textual values are invalid
     */
    public Transfer(
            TransferId id,
            AccountId sourceAccountId,
            AccountId destinationAccountId,
            Money amount,
            String description,
            String idempotencyKey,
            TransferStatus status,
            Instant createdAt,
            Instant completedAt,
            String failureReason
    ) {
        this.id = Objects.requireNonNull(
                id,
                "id must not be null"
        );

        this.sourceAccountId = Objects.requireNonNull(
                sourceAccountId,
                "sourceAccountId must not be null"
        );

        this.destinationAccountId = Objects.requireNonNull(
                destinationAccountId,
                "destinationAccountId must not be null"
        );

        this.amount = requirePositiveAmount(amount);
        this.description = normalizeDescription(description);
        this.idempotencyKey = requireIdempotencyKey(idempotencyKey);

        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt must not be null"
        );

        this.completedAt = completedAt;
        this.failureReason = failureReason;

        requireDifferentAccounts();
        requireConsistentState();
    }

    /**
     * Creates a pending transfer.
     *
     * @param id                   unique transfer identifier
     * @param sourceAccountId      account sending the funds
     * @param destinationAccountId account receiving the funds
     * @param amount               monetary amount to transfer
     * @param description          transfer description
     * @param idempotencyKey       key used to prevent duplicated processing
     * @param createdAt            transfer creation timestamp
     * @return newly created pending transfer
     */
    public static Transfer create(
            TransferId id,
            AccountId sourceAccountId,
            AccountId destinationAccountId,
            Money amount,
            String description,
            String idempotencyKey,
            Instant createdAt
    ) {
        return new Transfer(
                id,
                sourceAccountId,
                destinationAccountId,
                amount,
                description,
                idempotencyKey,
                TransferStatus.PENDING,
                createdAt,
                null,
                null
        );
    }

    /**
     * Marks the transfer as completed.
     *
     * @param completedAt completion timestamp
     * @throws IllegalStateException if the transfer is not pending
     */
    public void complete(Instant completedAt) {
        requirePending();

        this.status = TransferStatus.COMPLETED;
        this.completedAt = Objects.requireNonNull(
                completedAt,
                "completedAt must not be null"
        );
        this.failureReason = null;
    }

    /**
     * Marks the transfer as rejected because of a business rule.
     *
     * @param reason      safe rejection reason
     * @param completedAt rejection timestamp
     * @throws IllegalStateException if the transfer is not pending
     */
    public void reject(
            String reason,
            Instant completedAt
    ) {
        requirePending();

        this.status = TransferStatus.REJECTED;
        this.failureReason = requireReason(reason);
        this.completedAt = Objects.requireNonNull(
                completedAt,
                "completedAt must not be null"
        );
    }

    /**
     * Marks the transfer as failed because of an unexpected condition.
     *
     * @param reason      safe failure reason
     * @param completedAt failure timestamp
     * @throws IllegalStateException if the transfer is not pending
     */
    public void fail(
            String reason,
            Instant completedAt
    ) {
        requirePending();

        this.status = TransferStatus.FAILED;
        this.failureReason = requireReason(reason);
        this.completedAt = Objects.requireNonNull(
                completedAt,
                "completedAt must not be null"
        );
    }

    /**
     * Returns the transfer identifier.
     *
     * @return transfer identifier
     */
    public TransferId id() {
        return id;
    }

    /**
     * Returns the source account identifier.
     *
     * @return source account identifier
     */
    public AccountId sourceAccountId() {
        return sourceAccountId;
    }

    /**
     * Returns the destination account identifier.
     *
     * @return destination account identifier
     */
    public AccountId destinationAccountId() {
        return destinationAccountId;
    }

    /**
     * Returns the amount to transfer.
     *
     * @return immutable transfer amount
     */
    public Money amount() {
        return amount;
    }

    /**
     * Returns the optional transfer description.
     *
     * @return normalized description, possibly empty
     */
    public String description() {
        return description;
    }

    /**
     * Returns the idempotency key.
     *
     * @return idempotency key
     */
    public String idempotencyKey() {
        return idempotencyKey;
    }

    /**
     * Returns the current transfer status.
     *
     * @return transfer status
     */
    public TransferStatus status() {
        return status;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Returns the completion timestamp.
     *
     * @return completion timestamp, or {@code null} while pending
     */
    public Instant completedAt() {
        return completedAt;
    }

    /**
     * Returns the safe rejection or failure reason.
     *
     * @return reason, or {@code null} when no failure occurred
     */
    public String failureReason() {
        return failureReason;
    }

    private Money requirePositiveAmount(Money value) {
        Objects.requireNonNull(value, "amount must not be null");

        if (value.isZero()) {
            throw new IllegalArgumentException(
                    "transfer amount must be greater than zero"
            );
        }

        return value;
    }

    private String normalizeDescription(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }

    private String requireIdempotencyKey(String value) {
        Objects.requireNonNull(
                value,
                "idempotencyKey must not be null"
        );

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "idempotencyKey must not be blank"
            );
        }

        return normalized;
    }

    private String requireReason(String value) {
        Objects.requireNonNull(value, "reason must not be null");

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "reason must not be blank"
            );
        }

        return normalized;
    }

    private void requireDifferentAccounts() {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException(
                    "source and destination accounts must be different"
            );
        }
    }

    private void requirePending() {
        if (status != TransferStatus.PENDING) {
            throw new IllegalStateException(
                    "only pending transfers can change status"
            );
        }
    }

    private void requireConsistentState() {
        if (status == TransferStatus.PENDING) {
            if (completedAt != null || failureReason != null) {
                throw new IllegalArgumentException(
                        "pending transfer cannot have completion data"
                );
            }

            return;
        }

        if (completedAt == null) {
            throw new IllegalArgumentException(
                    "terminal transfer must have completedAt"
            );
        }

        if (status == TransferStatus.COMPLETED
                && failureReason != null) {
            throw new IllegalArgumentException(
                    "completed transfer cannot have failure reason"
            );
        }

        if ((status == TransferStatus.REJECTED
                || status == TransferStatus.FAILED)
                && (failureReason == null
                || failureReason.isBlank())) {

            throw new IllegalArgumentException(
                    "rejected or failed transfer must have a reason"
            );
        }
    }
}