package com.olek.banking.account.web;

import com.olek.banking.account.application.OpenAccountService;
import com.olek.banking.account.domain.Account;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Exposes HTTP operations related to bank accounts.
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final OpenAccountService openAccountService;

    /**
     * Creates the account controller.
     *
     * @param openAccountService account opening use case
     */
    public AccountController(
            OpenAccountService openAccountService
    ) {
        this.openAccountService = Objects.requireNonNull(
                openAccountService,
                "openAccountService must not be null"
        );
    }

    /**
     * Opens a new bank account.
     *
     * @param request validated account opening request
     * @return newly opened account
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpenAccountResponse openAccount(
            @Valid @RequestBody OpenAccountRequest request
    ) {
        Account account = openAccountService.open(
                AccountWebMapper.toCommand(request)
        );

        return AccountWebMapper.toResponse(account);
    }
}
