package com.olek.banking.account.infrastructure.persistence;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.olek.banking.account.domain.AccountId.generate;
import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAccountRepositoryTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T12:00:00Z");

    private AccountRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAccountRepository();
    }

    @Test
    void shouldSaveAndFindAccountById() {
        Account account = account("001-1234567890");

        repository.save(account);

        assertThat(repository.findById(account.id()))
                .contains(account);
    }

    @Test
    void shouldFindAccountByAccountNumber() {
        Account account = account("001-1234567890");
        repository.save(account);

        assertThat(
                repository.findByAccountNumber(
                        "001-1234567890"
                )
        ).contains(account);
    }

    @Test
    void shouldNormalizeAccountNumberWhenSearching() {
        Account account = account("001-1234567890");
        repository.save(account);

        assertThat(
                repository.findByAccountNumber(
                        " 001-1234567890 "
                )
        ).contains(account);
    }

    @Test
    void shouldReturnEmptyWhenAccountDoesNotExist() {
        assertThat(
                repository.findByAccountNumber(
                        "001-0000000000"
                )
        ).isEmpty();
    }

    @Test
    void shouldReturnAllAccounts() {
        repository.save(account("001-1234567890"));
        repository.save(account("001-0987654321"));

        assertThat(repository.findAll())
                .hasSize(2);
    }

    @Test
    void shouldReportExistingAccountNumber() {
        repository.save(account("001-1234567890"));

        assertThat(
                repository.existsByAccountNumber(
                        "001-1234567890"
                )
        ).isTrue();
    }

    @Test
    void shouldReplaceAccountWithSameIdentifier() {
        Account account = account("001-1234567890");

        repository.save(account);
        repository.save(account);

        assertThat(repository.findAll())
                .containsExactly(account);
    }

    private Account account(String accountNumber) {
        return Account.open(
                generate(),
                accountNumber,
                CurrencyCode.PEN,
                CREATED_AT
        );
    }
}
