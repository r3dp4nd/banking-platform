package com.olek.banking.transfer.domain;

/**
 * Represents the lifecycle status of a money transfer.
 */
public enum TransferStatus {

    /**
     * The transfer was created and is waiting to be processed.
     */
    PENDING,

    /**
     * The transfer completed successfully.
     */
    COMPLETED,

    /**
     * The transfer was rejected because a business rule was violated.
     */
    REJECTED,

    /**
     * The transfer failed because of an unexpected technical condition.
     */
    FAILED
}