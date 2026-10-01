package dev.ledgerflow.ledger.api;

import dev.ledgerflow.ledger.domain.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(UUID id, String name, String currency, BigDecimal balance) {
    static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getName(), account.getCurrency(), account.getCurrentBalance());
    }
}
