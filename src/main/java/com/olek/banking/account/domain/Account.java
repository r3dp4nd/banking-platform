package com.olek.banking.account.domain;

import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a bank account that owns a balance in a single currency.
 *
 * <p>The account protects its balance and status invariants through
 * domain operations such as deposit, debit, credit, block and close.</p>
 */
public class Account {

    private final AccountId id;
    private final String accountNumber;
    private final CurrencyCode currency;
    private final Instant createdAt;

    private Money balance;
    private AccountStatus status;

    /**
     * Creates a bank account.
     *
     * @param id             unique account identifier
     * @param accountNumber  externally visible account number
     * @param currency       currency managed by the account
     * @param initialBalance initial account balance
     * @param status         initial account status
     * @param createdAt      account creation timestamp
     * @throws NullPointerException     if any required value is {@code null}
     * @throws IllegalArgumentException if the account number is blank
     *                                  or the balance currency does not match
     */
    public Account(
            AccountId id,
            String accountNumber,
            CurrencyCode currency,
            Money initialBalance,
            AccountStatus status,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountNumber = requireAccountNumber(accountNumber);
        this.currency = Objects.requireNonNull(
                currency,
                "currency must not be null"
        );
        this.balance = requireMatchingCurrency(
                Objects.requireNonNull(
                        initialBalance,
                        "initialBalance must not be null"
                )
        );
        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt must not be null"
        );
    }

    /**
     * Opens a new active account with a zero balance.
     *
     * @param id            unique account identifier
     * @param accountNumber externally visible account number
     * @param currency      currency managed by the account
     * @param createdAt     account creation timestamp
     * @return newly opened account
     */
    public static Account open(
            AccountId id,
            String accountNumber,
            CurrencyCode currency,
            Instant createdAt
    ) {
        return new Account(
                id,
                accountNumber,
                currency,
                Money.zero(currency),
                AccountStatus.ACTIVE,
                createdAt
        );
    }

    /**
     * Deposits funds into the account.
     *
     * @param amount amount to deposit
     * @throws IllegalStateException    if the account is not active
     * @throws IllegalArgumentException if the currency does not match
     *                                  or the amount is zero
     */
    public void deposit(Money amount) {
        requireActive();
        requirePositive(amount);

        balance = balance.add(amount);
    }

    /**
     * Debits funds from the account.
     *
     * @param amount amount to debit
     * @throws IllegalStateException    if the account is not active
     * @throws IllegalArgumentException if the currency does not match,
     *                                  the amount is zero,
     *                                  or the balance is insufficient
     */
    public void debit(Money amount) {
        requireActive();
        requirePositive(amount);

        balance = balance.subtract(amount);
    }

    /**
     * Credits funds to the account as part of a money transfer.
     *
     * @param amount amount to credit
     * @throws IllegalStateException    if the account is not active
     * @throws IllegalArgumentException if the currency does not match
     *                                  or the amount is zero
     */
    public void credit(Money amount) {
        requireActive();
        requirePositive(amount);

        balance = balance.add(amount);
    }

    /**
     * Blocks the account.
     *
     * @throws IllegalStateException if the account is already closed
     */
    public void block() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException(
                    "closed account cannot be blocked"
            );
        }

        status = AccountStatus.BLOCKED;
    }

    /**
     * Reactivates a blocked account.
     *
     * @throws IllegalStateException if the account is closed
     */
    public void activate() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException(
                    "closed account cannot be activated"
            );
        }

        status = AccountStatus.ACTIVE;
    }

    /**
     * Permanently closes the account.
     *
     * @throws IllegalStateException if the account has a remaining balance
     */
    public void close() {
        if (!balance.isZero()) {
            throw new IllegalStateException(
                    "account with remaining balance cannot be closed"
            );
        }

        status = AccountStatus.CLOSED;
    }

    /**
     * Returns the account identifier.
     *
     * @return account identifier
     */
    public AccountId id() {
        return id;
    }

    /**
     * Returns the externally visible account number.
     *
     * @return account number
     */
    public String accountNumber() {
        return accountNumber;
    }

    /**
     * Returns the account currency.
     *
     * @return account currency
     */
    public CurrencyCode currency() {
        return currency;
    }

    /**
     * Returns the current account balance.
     *
     * @return immutable current balance
     */
    public Money balance() {
        return balance;
    }

    /**
     * Returns the current account status.
     *
     * @return account status
     */
    public AccountStatus status() {
        return status;
    }

    /**
     * Returns the account creation timestamp.
     *
     * @return account creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }

    private String requireAccountNumber(String value) {
        Objects.requireNonNull(value, "accountNumber must not be null");

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "accountNumber must not be blank"
            );
        }

        return normalized;
    }

    private Money requireMatchingCurrency(Money amount) {
        if (amount.currency() != currency) {
            throw new IllegalArgumentException(
                    "account and balance currencies must match"
            );
        }

        return amount;
    }

    private void requirePositive(Money amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        requireMatchingCurrency(amount);

        if (amount.isZero()) {
            throw new IllegalArgumentException(
                    "amount must be greater than zero"
            );
        }
    }

    private void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "account must be active"
            );
        }
    }
}
