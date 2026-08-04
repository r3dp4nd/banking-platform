package com.olek.banking.transfer.application;

import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.TransferRepository;
import com.olek.banking.transfer.domain.exception.TransferNotFoundException;

import java.util.Objects;

/**
 * Retrieves money transfers from the transfer repository.
 */
public final class GetTransferService {

    private final TransferRepository transferRepository;

    /**
     * Creates the transfer query service.
     *
     * @param transferRepository transfer persistence port
     */
    public GetTransferService(
            TransferRepository transferRepository
    ) {
        this.transferRepository = Objects.requireNonNull(
                transferRepository,
                "transferRepository must not be null"
        );
    }

    /**
     * Retrieves a transfer by its identifier.
     *
     * @param transferId transfer identifier
     * @return existing transfer
     * @throws NullPointerException      if the identifier is {@code null}
     * @throws TransferNotFoundException if the transfer does not exist
     */
    public Transfer getById(TransferId transferId) {
        Objects.requireNonNull(
                transferId,
                "transferId must not be null"
        );

        return transferRepository
                .findById(transferId)
                .orElseThrow(
                        () -> new TransferNotFoundException(
                                transferId
                        )
                );
    }
}