package com.olek.banking.transfer.web;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.shared.web.error.GlobalExceptionHandler;
import com.olek.banking.transfer.application.CreateTransferCommand;
import com.olek.banking.transfer.application.CreateTransferService;
import com.olek.banking.transfer.application.GetTransferService;
import com.olek.banking.transfer.domain.Transfer;
import com.olek.banking.transfer.domain.TransferId;
import com.olek.banking.transfer.domain.exception.IdempotencyKeyConflictException;
import com.olek.banking.transfer.domain.exception.TransferNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import({
        GlobalExceptionHandler.class,
        TransferControllerTest.FixedClockConfiguration.class
})
class TransferControllerTest {

    private static final Instant NOW =
            Instant.parse("2026-08-04T14:00:00Z");

    private static final String SOURCE_ACCOUNT_ID =
            "11111111-1111-1111-1111-111111111111";

    private static final String DESTINATION_ACCOUNT_ID =
            "22222222-2222-2222-2222-222222222222";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTransferService createTransferService;

    @MockitoBean
    private GetTransferService getTransferService;

    @Test
    void shouldCreateTransfer() throws Exception {
        Transfer transfer = completedTransfer();

        when(
                createTransferService.create(
                        any(CreateTransferCommand.class)
                )
        ).thenReturn(transfer);

        CreateTransferRequest request = validRequest();

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Idempotency-Key",
                                        "transfer-request-001"
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.transferId")
                                .value(transfer.id().toString())
                )
                .andExpect(
                        jsonPath("$.sourceAccountId")
                                .value(SOURCE_ACCOUNT_ID)
                )
                .andExpect(
                        jsonPath("$.destinationAccountId")
                                .value(DESTINATION_ACCOUNT_ID)
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(150.00)
                )
                .andExpect(
                        jsonPath("$.currency")
                                .value("PEN")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("COMPLETED")
                )
                .andExpect(
                        jsonPath("$.createdAt")
                                .value(NOW.toString())
                )
                .andExpect(
                        jsonPath("$.completedAt")
                                .value(NOW.toString())
                );
    }

    @Test
    void shouldRejectMissingIdempotencyKey()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                validRequest()
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("MISSING_REQUIRED_HEADER")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "required request header is missing"
                                )
                )
                .andExpect(
                        jsonPath("$.context.header")
                                .value("Idempotency-Key")
                );
    }

    @Test
    void shouldRejectBlankIdempotencyKey()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header("Idempotency-Key", "   ")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                validRequest()
                                        )
                                )
                )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_IDEMPOTENCY_KEY")
                );
    }

    @Test
    void shouldRejectInvalidSourceAccountId()
            throws Exception {

        CreateTransferRequest request =
                new CreateTransferRequest(
                        "not-a-uuid",
                        DESTINATION_ACCOUNT_ID,
                        new BigDecimal("150.00"),
                        CurrencyCode.PEN,
                        "Payment for services"
                );

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Idempotency-Key",
                                        "transfer-request-001"
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PARAMETER")
                )
                .andExpect(
                        jsonPath("$.context.parameter")
                                .value("sourceAccountId")
                )
                .andExpect(
                        jsonPath("$.context.value")
                                .value("not-a-uuid")
                );
    }

    @Test
    void shouldRejectInvalidAmount() throws Exception {
        CreateTransferRequest request =
                new CreateTransferRequest(
                        SOURCE_ACCOUNT_ID,
                        DESTINATION_ACCOUNT_ID,
                        BigDecimal.ZERO,
                        CurrencyCode.PEN,
                        "Payment for services"
                );

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Idempotency-Key",
                                        "transfer-request-001"
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.context.amount")
                                .value(
                                        "amount must be greater than zero"
                                )
                );
    }

    @Test
    void shouldReturnConflictForReusedIdempotencyKey()
            throws Exception {

        Transfer transfer = completedTransfer();

        when(
                createTransferService.create(
                        any(CreateTransferCommand.class)
                )
        ).thenThrow(
                new IdempotencyKeyConflictException(transfer)
        );

        mockMvc.perform(
                        post("/api/v1/transfers")
                                .header(
                                        "Idempotency-Key",
                                        "transfer-request-001"
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                validRequest()
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value("IDEMPOTENCY_KEY_CONFLICT")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "idempotency key was already used "
                                                + "with different data"
                                )
                )
                .andExpect(
                        jsonPath("$.context.existingTransferId")
                                .value(transfer.id().toString())
                );
    }

    @Test
    void shouldReturnTransferById() throws Exception {
        Transfer transfer = completedTransfer();

        when(getTransferService.getById(transfer.id()))
                .thenReturn(transfer);

        mockMvc.perform(
                        get(
                                "/api/v1/transfers/{transferId}",
                                transfer.id().toString()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.transferId")
                                .value(transfer.id().toString())
                )
                .andExpect(
                        jsonPath("$.sourceAccountId")
                                .value(SOURCE_ACCOUNT_ID)
                )
                .andExpect(
                        jsonPath("$.destinationAccountId")
                                .value(DESTINATION_ACCOUNT_ID)
                )
                .andExpect(
                        jsonPath("$.amount")
                                .value(150.00)
                )
                .andExpect(
                        jsonPath("$.currency")
                                .value("PEN")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("COMPLETED")
                )
                .andExpect(
                        jsonPath("$.failureReason")
                                .doesNotExist()
                );
    }

    @Test
    void shouldReturnNotFoundForMissingTransfer()
            throws Exception {

        TransferId transferId = TransferId.from(
                "33333333-3333-3333-3333-333333333333"
        );

        when(getTransferService.getById(transferId))
                .thenThrow(
                        new TransferNotFoundException(transferId)
                );

        mockMvc.perform(
                        get(
                                "/api/v1/transfers/{transferId}",
                                transferId.toString()
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("TRANSFER_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("transfer not found")
                )
                .andExpect(
                        jsonPath("$.context.transferId")
                                .value(transferId.toString())
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/transfers/"
                                                + transferId
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .value(NOW.toString())
                );
    }

    @Test
    void shouldRejectInvalidTransferId()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/transfers/{transferId}",
                                "not-a-uuid"
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PARAMETER")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "request parameter has an invalid format"
                                )
                )
                .andExpect(
                        jsonPath("$.context.parameter")
                                .value("transferId")
                )
                .andExpect(
                        jsonPath("$.context.value")
                                .value("not-a-uuid")
                );
    }

    private CreateTransferRequest validRequest() {
        return new CreateTransferRequest(
                SOURCE_ACCOUNT_ID,
                DESTINATION_ACCOUNT_ID,
                new BigDecimal("150.00"),
                CurrencyCode.PEN,
                "Payment for services"
        );
    }

    private Transfer completedTransfer() {
        Transfer transfer = Transfer.create(
                TransferId.from(
                        "33333333-3333-3333-3333-333333333333"
                ),
                AccountId.from(SOURCE_ACCOUNT_ID),
                AccountId.from(DESTINATION_ACCOUNT_ID),
                new Money(
                        new BigDecimal("150.00"),
                        CurrencyCode.PEN
                ),
                "Payment for services",
                "transfer-request-001",
                NOW
        );

        transfer.complete(NOW);
        return transfer;
    }

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        Clock testClock() {
            return Clock.fixed(
                    NOW,
                    ZoneOffset.UTC
            );
        }
    }
}