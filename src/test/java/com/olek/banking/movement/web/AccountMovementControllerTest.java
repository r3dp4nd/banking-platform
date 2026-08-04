package com.olek.banking.movement.web;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.movement.application.GetAccountMovementsService;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.shared.web.error.GlobalExceptionHandler;
import com.olek.banking.transfer.domain.TransferId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountMovementController.class)
@Import({
        GlobalExceptionHandler.class,
        AccountMovementControllerTest.FixedClockConfiguration.class
})
class AccountMovementControllerTest {

    private static final Instant NOW =
            Instant.parse("2026-08-04T15:00:00Z");

    private static final AccountId ACCOUNT_ID =
            AccountId.from(
                    "11111111-1111-1111-1111-111111111111"
            );

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAccountMovementsService getAccountMovementsService;

    @Test
    void shouldReturnAccountMovements() throws Exception {
        TransferId transferId = TransferId.from(
                "22222222-2222-2222-2222-222222222222"
        );

        AccountMovement deposit = AccountMovement.deposit(
                ACCOUNT_ID,
                money("500.00"),
                money("500.00"),
                NOW
        );

        AccountMovement debit = AccountMovement.debit(
                ACCOUNT_ID,
                transferId,
                money("150.00"),
                money("350.00"),
                NOW.plusSeconds(60)
        );

        when(
                getAccountMovementsService.getByAccountId(
                        ACCOUNT_ID
                )
        ).thenReturn(List.of(deposit, debit));

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}/movements",
                                ACCOUNT_ID
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[0].type")
                                .value("DEPOSIT")
                )
                .andExpect(
                        jsonPath("$[0].amount")
                                .value(500.00)
                )
                .andExpect(
                        jsonPath("$[0].balanceAfter")
                                .value(500.00)
                )
                .andExpect(
                        jsonPath("$[0].transferId")
                                .isEmpty()
                )
                .andExpect(
                        jsonPath("$[1].type")
                                .value("DEBIT")
                )
                .andExpect(
                        jsonPath("$[1].transferId")
                                .value(transferId.toString())
                )
                .andExpect(
                        jsonPath("$[1].balanceAfter")
                                .value(350.00)
                );
    }

    @Test
    void shouldReturnEmptyListWhenAccountHasNoMovements()
            throws Exception {

        when(
                getAccountMovementsService.getByAccountId(
                        ACCOUNT_ID
                )
        ).thenReturn(List.of());

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}/movements",
                                ACCOUNT_ID
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnNotFoundForMissingAccount()
            throws Exception {

        when(
                getAccountMovementsService.getByAccountId(
                        ACCOUNT_ID
                )
        ).thenThrow(
                new AccountNotFoundException(ACCOUNT_ID)
        );

        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}/movements",
                                ACCOUNT_ID
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("ACCOUNT_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.context.accountId")
                                .value(ACCOUNT_ID.toString())
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/accounts/"
                                                + ACCOUNT_ID
                                                + "/movements"
                                )
                );
    }

    @Test
    void shouldRejectInvalidAccountId() throws Exception {
        mockMvc.perform(
                        get(
                                "/api/v1/accounts/{accountId}/movements",
                                "not-a-uuid"
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PARAMETER")
                )
                .andExpect(
                        jsonPath("$.context.parameter")
                                .value("accountId")
                )
                .andExpect(
                        jsonPath("$.context.value")
                                .value("not-a-uuid")
                );
    }

    private static Money money(String amount) {
        return new Money(
                new BigDecimal(amount),
                CurrencyCode.PEN
        );
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