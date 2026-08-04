package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.account.infrastructure.persistence.InMemoryAccountRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.DomainErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetAccountServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T02:30:00Z");

    private AccountRepository accountRepository;
    private GetAccountService service;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        service = new GetAccountService(accountRepository);
    }

    @Test
    void shouldReturnExistingAccount() {
        Account account = Account.open(
                AccountId.generate(),
                "001-1234567890",
                CurrencyCode.PEN,
                CREATED_AT
        );

        accountRepository.save(account);

        Account result = service.getById(account.id());

        assertThat(result).isSameAs(account);
    }

    @Test
    void shouldRejectMissingAccount() {
        AccountId accountId = AccountId.generate();

        assertThatThrownBy(() ->
                service.getById(accountId)
        )
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("account not found")
                .satisfies(exception -> {
                    AccountNotFoundException domainException =
                            (AccountNotFoundException) exception;

                    assertThat(domainException.code())
                            .isEqualTo(
                                    DomainErrorCode.ACCOUNT_NOT_FOUND
                            );

                    assertThat(domainException.context())
                            .containsEntry(
                                    "accountId",
                                    accountId.toString()
                            );
                });
    }
}