package com.olek.banking.shared.domain;

/**
 * Identifies business rule violations produced by the domain.
 */
public enum DomainErrorCode {

    /**
     * The account is not active and cannot perform the requested operation.
     */
    ACCOUNT_NOT_ACTIVE,

    /**
     * The account does not contain enough funds.
     */
    INSUFFICIENT_BALANCE,

    /**
     * The monetary operation uses an incompatible currency.
     */
    CURRENCY_MISMATCH,

    /**
     * The monetary amount must be greater than zero.
     */
    INVALID_AMOUNT,

    /**
     * A closed account cannot transition to another operational status.
     */
    ACCOUNT_ALREADY_CLOSED,

    /**
     * An account with a remaining balance cannot be closed.
     */
    ACCOUNT_HAS_REMAINING_BALANCE,

    /**
     * The requested account number is already registered.
     */
    ACCOUNT_NUMBER_ALREADY_EXISTS,

    /**
     * The requested account does not exist.
     */
    ACCOUNT_NOT_FOUND,

    /**
     * The source and destination accounts must be different.
     */
    TRANSFER_ACCOUNTS_MUST_DIFFER,

    /**
     * The transfer amount must be greater than zero.
     */
    INVALID_TRANSFER_AMOUNT,

    /**
     * The transfer cannot perform the requested state transition.
     */
    INVALID_TRANSFER_STATE,

    /**
     * The idempotency key is missing or invalid.
     */
    INVALID_IDEMPOTENCY_KEY
}