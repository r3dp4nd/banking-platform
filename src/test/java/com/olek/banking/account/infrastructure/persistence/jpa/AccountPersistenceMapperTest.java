package com.olek.banking.account.infrastructure.persistence.jpa;

import com.olek.banking.account.domain.Account;
import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountStatus;
import com.olek.banking.shared.domain.CurrencyCode;
import com.olek.banking.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountPersistenceMapperTest {

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-04T16:00:00Z");

    @Test
    void shouldMapDomainAccountToJpaEntity() {
        Account account = account();

        AccountJpaEntity entity =
                AccountPersistenceMapper.toEntity(account);

        assertThat(entity.getId())
                .isEqualTo(ACCOUNT_ID);

        assertThat(entity.getAccountNumber())
                .isEqualTo("001-1234567890");

        assertThat(entity.getCurrency())
                .isEqualTo(CurrencyCode.PEN);

        assertThat(entity.getBalance())
                .isEqualByComparingTo("500.00");

        assertThat(entity.getStatus())
                .isEqualTo(AccountStatus.ACTIVE);

        assertThat(entity.getCreatedAt())
                .isEqualTo(CREATED_AT);
    }

    @Test
    void shouldMapJpaEntityToDomainAccount() {
        AccountJpaEntity entity = new AccountJpaEntity(
                ACCOUNT_ID,
                "001-1234567890",
                CurrencyCode.PEN,
                new BigDecimal("500.00"),
                AccountStatus.BLOCKED,
                CREATED_AT
        );

        Account account =
                AccountPersistenceMapper.toDomain(entity);

        assertThat(account.id())
                .isEqualTo(new AccountId(ACCOUNT_ID));

        assertThat(account.accountNumber())
                .isEqualTo("001-1234567890");

        assertThat(account.currency())
                .isEqualTo(CurrencyCode.PEN);

        assertThat(account.balance())
                .isEqualTo(
                        new Money(
                                new BigDecimal("500.00"),
                                CurrencyCode.PEN
                        )
                );

        assertThat(account.status())
                .isEqualTo(AccountStatus.BLOCKED);

        assertThat(account.createdAt())
                .isEqualTo(CREATED_AT);
    }

    @Test
    void shouldPreserveAccountWhenMappingRoundTrip() {
        Account original = account();

        Account result = AccountPersistenceMapper.toDomain(
                AccountPersistenceMapper.toEntity(original)
        );

        assertThat(result.id())
                .isEqualTo(original.id());

        assertThat(result.accountNumber())
                .isEqualTo(original.accountNumber());

        assertThat(result.currency())
                .isEqualTo(original.currency());

        assertThat(result.balance())
                .isEqualTo(original.balance());

        assertThat(result.status())
                .isEqualTo(original.status());

        assertThat(result.createdAt())
                .isEqualTo(original.createdAt());
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