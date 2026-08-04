package com.olek.banking.account.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.shared.domain.CurrencyCode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents the relational persistence model of a bank account.
 *
 * <p>This entity belongs exclusively to the infrastructure layer and must not
 * contain business behavior.</p>
 */
@Entity
@Table(name = "bank_account")
public class AccountJpaEntity {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @Column(
            name = "account_number",
            nullable = false,
            length = 32,
            unique = true
    )
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    private CurrencyCode currency;

    @Column(
            name = "balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private AccountStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    /**
     * Creates an empty entity required by JPA.
     */
    protected AccountJpaEntity() {
    }

    /**
     * Creates an account persistence entity.
     *
     * @param id            account identifier
     * @param accountNumber externally visible account number
     * @param currency      account currency
     * @param balance       current account balance
     * @param status        account status
     * @param createdAt     account creation timestamp
     */
    public AccountJpaEntity(
            UUID id,
            String accountNumber,
            CurrencyCode currency,
            BigDecimal balance,
            AccountStatus status,
            Instant createdAt
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Returns the entity identifier.
     *
     * @return account identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the account number.
     *
     * @return externally visible account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Returns the account currency.
     *
     * @return account currency
     */
    public CurrencyCode getCurrency() {
        return currency;
    }

    /**
     * Returns the persisted balance.
     *
     * @return current account balance
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Returns the account status.
     *
     * @return account status
     */
    public AccountStatus getStatus() {
        return status;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return account creation timestamp
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
