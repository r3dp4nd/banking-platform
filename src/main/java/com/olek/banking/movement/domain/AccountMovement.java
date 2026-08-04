package com.olek.banking.movement.domain;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.transfer.domain.TransferId;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an immutable financial movement recorded for a bank account.
 *
 * <p>The movement captures the amount involved and the resulting account
 * balance immediately after the operation.</p>
 */
public final class AccountMovement {

    private final MovementId id;
    private final AccountId accountId;
    private final TransferId transferId;
    private final MovementType type;
    private final Money amount;
    private final Money balanceAfter;
    private final Instant createdAt;

    /**
     * Creates an immutable account movement.
     *
     * @param id           unique movement identifier
     * @param accountId    affected account identifier
     * @param transferId   related transfer identifier, or {@code null} for
     *                     movements not produced by a transfer
     * @param type         movement type
     * @param amount       amount involved in the movement
     * @param balanceAfter account balance after applying the movement
     * @param createdAt    movement creation timestamp
     * @throws NullPointerException     if a required value is {@code null}
     * @throws IllegalArgumentException if the amount is zero or the
     *                                  currencies do not match
     */
    public AccountMovement(
            MovementId id,
            AccountId accountId,
            TransferId transferId,
            MovementType type,
            Money amount,
            Money balanceAfter,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(
                id,
                "id must not be null"
        );

        this.accountId = Objects.requireNonNull(
                accountId,
                "accountId must not be null"
        );

        this.transferId = transferId;

        this.type = Objects.requireNonNull(
                type,
                "type must not be null"
        );

        this.amount = requirePositiveAmount(amount);

        this.balanceAfter = Objects.requireNonNull(
                balanceAfter,
                "balanceAfter must not be null"
        );

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt must not be null"
        );

        requireMatchingCurrencies();
        requireTransferReference();
    }

    /**
     * Creates a deposit movement.
     *
     * @param accountId    affected account
     * @param amount       deposited amount
     * @param balanceAfter resulting account balance
     * @param createdAt    creation timestamp
     * @return immutable deposit movement
     */
    public static AccountMovement deposit(
            AccountId accountId,
            Money amount,
            Money balanceAfter,
            Instant createdAt
    ) {
        return new AccountMovement(
                MovementId.generate(),
                accountId,
                null,
                MovementType.DEPOSIT,
                amount,
                balanceAfter,
                createdAt
        );
    }

    /**
     * Creates a debit movement associated with a transfer.
     *
     * @param accountId    affected source account
     * @param transferId   related transfer
     * @param amount       debited amount
     * @param balanceAfter resulting account balance
     * @param createdAt    creation timestamp
     * @return immutable debit movement
     */
    public static AccountMovement debit(
            AccountId accountId,
            TransferId transferId,
            Money amount,
            Money balanceAfter,
            Instant createdAt
    ) {
        return new AccountMovement(
                MovementId.generate(),
                accountId,
                transferId,
                MovementType.DEBIT,
                amount,
                balanceAfter,
                createdAt
        );
    }

    /**
     * Creates a credit movement associated with a transfer.
     *
     * @param accountId    affected destination account
     * @param transferId   related transfer
     * @param amount       credited amount
     * @param balanceAfter resulting account balance
     * @param createdAt    creation timestamp
     * @return immutable credit movement
     */
    public static AccountMovement credit(
            AccountId accountId,
            TransferId transferId,
            Money amount,
            Money balanceAfter,
            Instant createdAt
    ) {
        return new AccountMovement(
                MovementId.generate(),
                accountId,
                transferId,
                MovementType.CREDIT,
                amount,
                balanceAfter,
                createdAt
        );
    }

    /**
     * Returns the movement identifier.
     *
     * @return movement identifier
     */
    public MovementId id() {
        return id;
    }

    /**
     * Returns the affected account identifier.
     *
     * @return account identifier
     */
    public AccountId accountId() {
        return accountId;
    }

    /**
     * Returns the related transfer identifier.
     *
     * @return transfer identifier, or {@code null} for deposits
     */
    public TransferId transferId() {
        return transferId;
    }

    /**
     * Returns the movement type.
     *
     * @return movement type
     */
    public MovementType type() {
        return type;
    }

    /**
     * Returns the amount involved in the movement.
     *
     * @return immutable movement amount
     */
    public Money amount() {
        return amount;
    }

    /**
     * Returns the resulting account balance.
     *
     * @return immutable account balance after the movement
     */
    public Money balanceAfter() {
        return balanceAfter;
    }

    /**
     * Returns the movement creation timestamp.
     *
     * @return movement creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    private Money requirePositiveAmount(Money value) {
        Objects.requireNonNull(value, "amount must not be null");

        if (value.isZero()) {
            throw new IllegalArgumentException(
                    "movement amount must be greater than zero"
            );
        }

        return value;
    }

    private void requireMatchingCurrencies() {
        if (amount.currency() != balanceAfter.currency()) {
            throw new IllegalArgumentException(
                    "movement and balance currencies must match"
            );
        }
    }

    private void requireTransferReference() {
        if (type == MovementType.DEPOSIT && transferId != null) {
            throw new IllegalArgumentException(
                    "deposit movement cannot reference a transfer"
            );
        }

        if ((type == MovementType.DEBIT
                || type == MovementType.CREDIT)
                && transferId == null) {
            throw new IllegalArgumentException(
                    "transfer movement must reference a transfer"
            );
        }
    }
}