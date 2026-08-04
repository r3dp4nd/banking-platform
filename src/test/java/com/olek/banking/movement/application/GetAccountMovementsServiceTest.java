package com.olek.banking.movement.application;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.account.infrastructure.persistence.InMemoryAccountRepository;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;
import com.olek.banking.movement.infrastructure.persistence.InMemoryAccountMovementRepository;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetAccountMovementsServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T15:00:00Z");

    private AccountRepository accountRepository;
    private AccountMovementRepository movementRepository;
    private GetAccountMovementsService service;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        movementRepository =
                new InMemoryAccountMovementRepository();

        service = new GetAccountMovementsService(
                accountRepository,
                movementRepository
        );
    }

    @Test
    void shouldReturnAccountMovementsInChronologicalOrder() {
        Account account = account();
        accountRepository.save(account);

        AccountMovement second = AccountMovement.deposit(
                account.id(),
                money("50.00"),
                money("150.00"),
                Instant.parse("2026-08-04T15:02:00Z")
        );

        AccountMovement first = AccountMovement.deposit(
                account.id(),
                money("100.00"),
                money("100.00"),
                Instant.parse("2026-08-04T15:01:00Z")
        );

        movementRepository.save(second);
        movementRepository.save(first);

        List<AccountMovement> result =
                service.getByAccountId(account.id());

        assertThat(result)
                .containsExactly(first, second);
    }

    @Test
    void shouldReturnEmptyListWhenAccountHasNoMovements() {
        Account account = account();
        accountRepository.save(account);

        assertThat(service.getByAccountId(account.id()))
                .isEmpty();
    }

    @Test
    void shouldRejectMissingAccount() {
        AccountId accountId = AccountId.generate();

        assertThatThrownBy(() ->
                service.getByAccountId(accountId)
        )
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("account not found");
    }

    @Test
    void shouldRejectNullAccountId() {
        assertThatThrownBy(() ->
                service.getByAccountId(null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("accountId must not be null");
    }

    private Account account() {
        return Account.open(
                AccountId.generate(),
                "001-1234567890",
                CurrencyCode.PEN,
                CREATED_AT
        );
    }

    private Money money(String amount) {
        return new Money(
                new BigDecimal(amount),
                CurrencyCode.PEN
        );
    }
}