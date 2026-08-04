package com.olek.banking.account.domain;

/**
 * Represents the operational status of a bank account.
 */
public enum AccountStatus {

    /**
     * The account can send and receive funds.
     */
    ACTIVE,

    /**
     * The account cannot send or receive funds.
     */
    BLOCKED,

    /**
     * The account is permanently closed.
     */
    CLOSED
}