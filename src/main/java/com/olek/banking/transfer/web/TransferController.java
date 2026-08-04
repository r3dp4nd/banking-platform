package com.olek.banking.transfer.web;

import com.olek.banking.transfer.application.CreateTransferService;
import com.olek.banking.transfer.domain.Transfer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Exposes HTTP operations related to money transfers.
 */
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private static final String IDEMPOTENCY_KEY_HEADER =
            "Idempotency-Key";

    private final CreateTransferService createTransferService;

    /**
     * Creates the transfer controller.
     *
     * @param createTransferService transfer creation use case
     */
    public TransferController(
            CreateTransferService createTransferService
    ) {
        this.createTransferService = Objects.requireNonNull(
                createTransferService,
                "createTransferService must not be null"
        );
    }

    /**
     * Creates and processes an internal money transfer.
     *
     * @param idempotencyKey key used to prevent duplicated processing
     * @param request        validated transfer request
     * @return created or previously processed transfer
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse createTransfer(
            @RequestHeader(IDEMPOTENCY_KEY_HEADER)
            String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest request
    ) {
        Transfer transfer = createTransferService.create(
                TransferWebMapper.toCommand(
                        request,
                        idempotencyKey
                )
        );

        return TransferWebMapper.toResponse(transfer);
    }
}