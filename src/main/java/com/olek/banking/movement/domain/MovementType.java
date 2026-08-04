package com.olek.banking.movement.domain;

/**
 * Represents the financial effect recorded for a bank account.
 */
public enum MovementType {

    /**
     * Funds added through the test deposit operation.
     */
    DEPOSIT,

    /**
     * Funds removed from a transfer source account.
     */
    DEBIT,

    /**
     * Funds added to a transfer destination account.
     */
    CREDIT
}