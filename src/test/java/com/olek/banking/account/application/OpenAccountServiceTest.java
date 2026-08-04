package com.olek.banking.account.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.account.domain.exception.AccountNumberAlreadyExistsException;
import com.olek.banking.account.infrastructure.persistence.InMemoryAccountRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAccountServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-04T02:00:00Z");

    private AccountRepository accountRepository;
    private OpenAccountService service;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();

        Clock clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        service = new OpenAccountService(
                accountRepository,
                clock
        );
    }

    @Test
    void shouldOpenAndPersistAccount() {
        OpenAccountCommand command =
                new OpenAccountCommand(
                        "001-1234567890",
                        CurrencyCode.PEN
                );

        Account account = service.open(command);

        assertThat(account.id()).isNotNull();

        assertThat(account.accountNumber())
                .isEqualTo("001-1234567890");

        assertThat(account.currency())
                .isEqualTo(CurrencyCode.PEN);

        assertThat(account.balance())
                .isEqualTo(Money.zero(CurrencyCode.PEN));

        assertThat(account.status())
                .isEqualTo(AccountStatus.ACTIVE);

        assertThat(account.createdAt())
                .isEqualTo(NOW);

        assertThat(accountRepository.findById(account.id()))
                .contains(account);
    }

    @Test
    void shouldNormalizeAccountNumber() {
        Account account = service.open(
                new OpenAccountCommand(
                        " 001-1234567890 ",
                        CurrencyCode.USD
                )
        );

        assertThat(account.accountNumber())
                .isEqualTo("001-1234567890");
    }

    @Test
    void shouldRejectDuplicatedAccountNumber() {
        OpenAccountCommand command =
                new OpenAccountCommand(
                        "001-1234567890",
                        CurrencyCode.PEN
                );

        service.open(command);

        assertThatThrownBy(() -> service.open(command))
                .isInstanceOf(
                        AccountNumberAlreadyExistsException.class
                )
                .hasMessage("account number already exists")
                .satisfies(exception -> {
                    AccountNumberAlreadyExistsException
                            domainException =
                            (AccountNumberAlreadyExistsException)
                                    exception;

                    assertThat(domainException.code())
                            .isEqualTo(
                                    DomainErrorCode
                                            .ACCOUNT_NUMBER_ALREADY_EXISTS
                            );

                    assertThat(domainException.context())
                            .containsEntry(
                                    "accountNumber",
                                    "001-1234567890"
                            );
                });
    }

    @Test
    void shouldRejectBlankAccountNumber() {
        assertThatThrownBy(() ->
                new OpenAccountCommand(
                        "   ",
                        CurrencyCode.PEN
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "accountNumber must not be blank"
                );
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(() -> service.open(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("command must not be null");
    }
}