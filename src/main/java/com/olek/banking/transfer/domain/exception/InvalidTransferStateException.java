package com.olek.banking.transfer.domain.exception;

import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferStatus;

import java.util.Map;

/**
 * Indicates that a transfer cannot perform a requested state transition.
 */
public final class InvalidTransferStateException
        extends DomainException {

    /**
     * Creates the exception.
     *
     * @param transferId    transfer that rejected the transition
     * @param currentStatus current transfer status
     */
    public InvalidTransferStateException(
            TransferId transferId,
            TransferStatus currentStatus
    ) {
        super(
                DomainErrorCode.INVALID_TRANSFER_STATE,
                "only pending transfers can change status",
                Map.of(
                        "transferId", transferId.toString(),
                        "currentStatus", currentStatus.name()
                )
        );
    }
}