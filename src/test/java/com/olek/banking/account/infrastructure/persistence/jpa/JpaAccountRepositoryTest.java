package com.olek.banking.account.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JpaAccountRepositoryTest {

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T16:00:00Z");

    private SpringDataAccountJpaRepository springDataRepository;
    private JpaAccountRepository repository;

    @BeforeEach
    void setUp() {
        springDataRepository = mock(
                SpringDataAccountJpaRepository.class
        );

        repository = new JpaAccountRepository(
                springDataRepository
        );
    }

    @Test
    void shouldSaveDomainAccount() {
        Account account = account();

        AccountJpaEntity persistedEntity =
                AccountPersistenceMapper.toEntity(account);

        when(springDataRepository.save(
                any(
                        AccountJpaEntity.class
                )
        )).thenReturn(persistedEntity);

        Account result = repository.save(account);

        ArgumentCaptor<AccountJpaEntity> captor =
                ArgumentCaptor.forClass(
                        AccountJpaEntity.class
                );

        verify(springDataRepository).save(captor.capture());

        assertThat(captor.getValue().getId())
                .isEqualTo(ACCOUNT_ID);

        assertThat(result.id())
                .isEqualTo(account.id());

        assertThat(result.balance())
                .isEqualTo(account.balance());
    }

    @Test
    void shouldFindAccountById() {
        AccountJpaEntity entity =
                AccountPersistenceMapper.toEntity(account());

        when(springDataRepository.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(entity));

        assertThat(
                repository.findById(
                        new AccountId(ACCOUNT_ID)
                )
        )
                .get()
                .extracting(Account::accountNumber)
                .isEqualTo("001-1234567890");
    }

    @Test
    void shouldNormalizeAccountNumberWhenSearching() {
        AccountJpaEntity entity =
                AccountPersistenceMapper.toEntity(account());

        when(
                springDataRepository.findByAccountNumber(
                        "001-1234567890"
                )
        ).thenReturn(Optional.of(entity));

        assertThat(
                repository.findByAccountNumber(
                        " 001-1234567890 "
                )
        ).isPresent();

        verify(springDataRepository)
                .findByAccountNumber(
                        "001-1234567890"
                );
    }

    @Test
    void shouldReturnAllAccounts() {
        AccountJpaEntity entity =
                AccountPersistenceMapper.toEntity(account());

        when(springDataRepository.findAll())
                .thenReturn(List.of(entity));

        assertThat(repository.findAll())
                .singleElement()
                .extracting(Account::id)
                .isEqualTo(new AccountId(ACCOUNT_ID));
    }

    private Account account() {
        return new Account(
                new AccountId(ACCOUNT_ID),
                "001-1234567890",
                CurrencyCode.PEN,
                new Money(
                        new BigDecimal("500.00"),
                        CurrencyCode.PEN
                ),
                AccountStatus.ACTIVE,
                CREATED_AT
        );
    }
}