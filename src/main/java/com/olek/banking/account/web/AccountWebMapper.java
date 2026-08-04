package com.olek.banking.account.web;

import com.olek.banking.account.application.DepositFundsCommand;
import com.olek.banking.account.application.OpenAccountCommand;
import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.shared.domain.Money;
import com.olek.banking.shared.web.error.InvalidRequestParameterException;

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
    static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.id().toString(),
                account.accountNumber(),
                account.currency(),
                account.balance().amount(),
                account.status().name(),
                account.createdAt()
        );
    }

    /**
     * Maps a textual HTTP path value to an account identifier.
     *
     * @param value textual account identifier
     * @return parsed account identifier
     * @throws InvalidRequestParameterException if the value is not a UUID
     */
    static AccountId toAccountId(String value) {
        try {
            return AccountId.from(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestParameterException(
                    "accountId",
                    value
            );
        }
    }

    /**
     * Maps a deposit HTTP request to an application command.
     *
     * @param accountId textual account identifier
     * @param request   deposit request
     * @return deposit funds command
     * @throws InvalidRequestParameterException if the account identifier is
     *                                          invalid
     */
    static DepositFundsCommand toDepositCommand(
            String accountId,
            DepositFundsRequest request
    ) {
        return new DepositFundsCommand(
                toAccountId(accountId),
                new Money(
                        request.amount(),
                        request.currency()
                )
        );
    }
}