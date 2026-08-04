package com.olek.banking.account.web;

import com.olek.banking.account.application.OpenAccountCommand;
import com.olek.banking.account.application.OpenAccountService;
import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.exception.AccountNumberAlreadyExistsException;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.shared.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({
        GlobalExceptionHandler.class,
        AccountControllerTest.FixedClockConfiguration.class
})
class AccountControllerTest {

    private static final Instant NOW =
            Instant.parse("2026-08-04T02:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OpenAccountService openAccountService;

    @Test
    void shouldOpenAccount() throws Exception {
        Account account = new Account(
                AccountId.from(
                        "b8f62817-7a18-4c39-b750-8d608a245f52"
                ),
                "001-1234567890",
                CurrencyCode.PEN,
                Money.zero(CurrencyCode.PEN),
                com.olek.banking.account.domain.AccountStatus.ACTIVE,
                NOW
        );

        when(openAccountService.open(any(OpenAccountCommand.class)))
                .thenReturn(account);

        OpenAccountRequest request = new OpenAccountRequest(
                "001-1234567890",
                CurrencyCode.PEN
        );

        mockMvc.perform(
                        post("/api/v1/accounts")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.accountId")
                                .value(
                                        "b8f62817-7a18-4c39-b750-8d608a245f52"
                                )
                )
                .andExpect(
                        jsonPath("$.accountNumber")
                                .value("001-1234567890")
                )
                .andExpect(
                        jsonPath("$.currency")
                                .value("PEN")
                )
                .andExpect(
                        jsonPath("$.balance")
                                .value(0.00)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                );
    }

    @Test
    void shouldRejectInvalidAccountNumber() throws Exception {
        OpenAccountRequest request = new OpenAccountRequest(
                "123",
                CurrencyCode.PEN
        );

        mockMvc.perform(
                        post("/api/v1/accounts")
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
                        jsonPath("$.message")
                                .value("request validation failed")
                )
                .andExpect(
                        jsonPath("$.context.accountNumber")
                                .value(
                                        "accountNumber must use the format "
                                                + "000-0000000000"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/v1/accounts")
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .value(NOW.toString())
                );
    }

    @Test
    void shouldReturnConflictForDuplicatedAccountNumber()
            throws Exception {

        when(openAccountService.open(any(OpenAccountCommand.class)))
                .thenThrow(
                        new AccountNumberAlreadyExistsException(
                                "001-1234567890"
                        )
                );

        OpenAccountRequest request = new OpenAccountRequest(
                "001-1234567890",
                CurrencyCode.PEN
        );

        mockMvc.perform(
                        post("/api/v1/accounts")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "ACCOUNT_NUMBER_ALREADY_EXISTS"
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "account number already exists"
                                )
                )
                .andExpect(
                        jsonPath("$.context.accountNumber")
                                .value("001-1234567890")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/v1/accounts")
                );
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(
                        post("/api/v1/accounts")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "accountNumber":
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("MALFORMED_REQUEST")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "request body is missing or malformed"
                                )
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