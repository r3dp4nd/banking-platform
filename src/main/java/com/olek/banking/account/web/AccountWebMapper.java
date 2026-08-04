package com.olek.banking.account.web;

import com.olek.banking.account.application.OpenAccountCommand;
import com.olek.banking.account.domain.Account;

/**
 * Maps account web models to application and domain models.
 */
final class AccountWebMapper {

    private AccountWebMapper() {
    }

    /**
     * Maps an HTTP request to an account opening command.
     *
     * @param request account opening HTTP request
     * @return application command
     */
    static OpenAccountCommand toCommand(
            OpenAccountRequest request
    ) {
        return new OpenAccountCommand(
                request.accountNumber(),
                request.currency()
        );
    }

    /**
     * Maps an account to its HTTP response representation.
     *
     * @param account account to map
     * @return account opening response
     */
    static OpenAccountResponse toResponse(Account account) {
        return new OpenAccountResponse(
                account.id().toString(),
                account.accountNumber(),
                account.currency(),
                account.balance().amount(),
                account.status().name(),
                account.createdAt()
        );
    }
}