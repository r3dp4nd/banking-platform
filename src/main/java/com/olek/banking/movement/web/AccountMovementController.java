package com.olek.banking.movement.web;

import com.olek.banking.movement.application.GetAccountMovementsService;
import com.olek.banking.movement.domain.AccountMovement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Exposes HTTP operations related to account movements.
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountMovementController {

    private final GetAccountMovementsService getAccountMovementsService;

    /**
     * Creates the account movement controller.
     *
     * @param getAccountMovementsService account movement query use case
     */
    public AccountMovementController(
            GetAccountMovementsService getAccountMovementsService
    ) {
        this.getAccountMovementsService = Objects.requireNonNull(
                getAccountMovementsService,
                "getAccountMovementsService must not be null"
        );
    }

    /**
     * Retrieves the movements associated with a bank account.
     *
     * @param accountId textual account identifier
     * @return account movements ordered from oldest to newest
     */
    @GetMapping("/{accountId}/movements")
    public List<AccountMovementResponse> getAccountMovements(
            @PathVariable String accountId
    ) {
        List<AccountMovement> movements =
                getAccountMovementsService.getByAccountId(
                        AccountMovementWebMapper.toAccountId(
                                accountId
                        )
                );

        return AccountMovementWebMapper.toResponses(movements);
    }
}