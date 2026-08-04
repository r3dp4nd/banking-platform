package com.olek.banking.transfer.domain.exception;

import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.transfer.domain.TransferId;

import java.util.Map;

/**
 * Indicates that a transfer could not be found.
 */
public final class TransferNotFoundException
        extends DomainException {

    /**
     * Creates the exception.
     *
     * @param transferId missing transfer identifier
     */
    public TransferNotFoundException(TransferId transferId) {
        super(
                DomainErrorCode.TRANSFER_NOT_FOUND,
                "transfer not found",
                Map.of(
                        "transferId",
                        transferId.toString()
                )
        );
    }
}