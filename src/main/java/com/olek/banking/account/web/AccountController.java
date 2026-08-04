package com.olek.banking.account.web;

import com.olek.banking.account.application.DepositFundsService;
import com.olek.banking.account.application.GetAccountService;
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
    private final GetAccountService getAccountService;
    private final DepositFundsService depositFundsService;

    /**
     * Creates the account controller.
     *
     * @param openAccountService account opening use case
     */
    public AccountController(
            OpenAccountService openAccountService,
            GetAccountService getAccountService,
            DepositFundsService depositFundsService
    ) {
        this.openAccountService = Objects.requireNonNull(
                openAccountService,
                "openAccountService must not be null"
        );

        this.getAccountService = Objects.requireNonNull(
                getAccountService,
                "getAccountService must not be null"
        );

        this.depositFundsService = Objects.requireNonNull(
                depositFundsService,
                "depositFundsService must not be null"
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
    public AccountResponse openAccount(
            @Valid @RequestBody OpenAccountRequest request
    ) {
        Account account = openAccountService.open(
                AccountWebMapper.toCommand(request)
        );

        return AccountWebMapper.toResponse(account);
    }

    /**
     * Retrieves a bank account by its identifier.
     *
     * @param accountId textual account identifier
     * @return existing bank account
     */
    @GetMapping("/{accountId}")
    public AccountResponse getAccount(
            @PathVariable String accountId
    ) {
        Account account = getAccountService.getById(
                AccountWebMapper.toAccountId(accountId)
        );

        return AccountWebMapper.toResponse(account);
    }

    /**
     * Deposits test funds into an existing bank account.
     *
     * @param accountId textual account identifier
     * @param request   validated deposit request
     * @return updated bank account
     */
    @PostMapping("/{accountId}/deposits")
    public AccountResponse depositFunds(
            @PathVariable String accountId,
            @Valid @RequestBody DepositFundsRequest request
    ) {
        Account account = depositFundsService.deposit(
                AccountWebMapper.toDepositCommand(
                        accountId,
                        request
                )
        );

        return AccountWebMapper.toResponse(account);
    }
}
