package com.olek.banking.movement.application;

import com.olek.banking.account.domain.AccountId;
import com.olek.banking.account.domain.AccountRepository;
import com.olek.banking.account.domain.exception.AccountNotFoundException;
import com.olek.banking.movement.domain.AccountMovement;
import com.olek.banking.movement.domain.AccountMovementRepository;

import java.util.List;
import java.util.Objects;

/**
 * Retrieves the financial movements associated with a bank account.
 */
public final class GetAccountMovementsService {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository movementRepository;

    /**
     * Creates the account movement query service.
     *
     * @param accountRepository account persistence port
     * @param movementRepository account movement persistence port
     */
    public GetAccountMovementsService(
            AccountRepository accountRepository,
            AccountMovementRepository movementRepository
    ) {
        this.accountRepository = Objects.requireNonNull(
                accountRepository,
                "accountRepository must not be null"
        );

        this.movementRepository = Objects.requireNonNull(
                movementRepository,
                "movementRepository must not be null"
        );
    }

    /**
     * Retrieves the movements associated with an existing account.
     *
     * @param accountId account identifier
     * @return immutable movements ordered from oldest to newest
     * @throws NullPointerException if the account identifier is {@code null}
     * @throws AccountNotFoundException if the account does not exist
     */
    public List<AccountMovement> getByAccountId(
            AccountId accountId
    ) {
        Objects.requireNonNull(
                accountId,
                "accountId must not be null"
        );

        if (accountRepository.findById(accountId).isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }

        return movementRepository.findByAccountId(accountId);
    }
}