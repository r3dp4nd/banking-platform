package com.olek.banking.transfer.web;

import com.olek.banking.transfer.application.CreateTransferResult;
import com.olek.banking.transfer.application.CreateTransferService;
import com.olek.banking.transfer.application.GetTransferService;
import com.olek.banking.transfer.domain.Transfer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final GetTransferService getTransferService;

    /**
     * Creates the transfer controller.
     *
     * @param createTransferService transfer creation use case
     */
    public TransferController(
            CreateTransferService createTransferService,
            GetTransferService getTransferService
    ) {
        this.createTransferService = Objects.requireNonNull(
                createTransferService,
                "createTransferService must not be null"
        );

        this.getTransferService = Objects.requireNonNull(
                getTransferService,
                "getTransferService must not be null"
        );
    }

    /**
     * Creates or idempotently recovers an internal transfer.
     *
     * @param idempotencyKey key used to prevent duplicated processing
     * @param request        validated transfer request
     * @return created or previously processed transfer
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TransferResponse> createTransfer(
            @RequestHeader(IDEMPOTENCY_KEY_HEADER)
            String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest request
    ) {
        CreateTransferResult result = createTransferService.create(
                TransferWebMapper.toCommand(
                        request,
                        idempotencyKey
                )
        );

        TransferResponse response =
                TransferWebMapper.toResponse(
                        result.transfer()
                );

        if (result.wasCreated()) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a money transfer by its identifier.
     *
     * @param transferId textual transfer identifier
     * @return existing transfer
     */
    @GetMapping("/{transferId}")
    public TransferResponse getTransfer(
            @PathVariable String transferId
    ) {
        Transfer transfer = getTransferService.getById(
                TransferWebMapper.toTransferId(transferId)
        );

        return TransferWebMapper.toResponse(transfer);
    }
}