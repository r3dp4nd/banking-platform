package com.olek.banking.account.web;

import com.olek.banking.account.application.OpenAccountService;
import com.olek.banking.shared.domain.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OpenAccountService openAccountService;

    @Test
    void shouldRejectInvalidAccountNumber() throws Exception {
        OpenAccountRequest request =
                new OpenAccountRequest(
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
                .andExpect(status().isBadRequest());
    }
}
