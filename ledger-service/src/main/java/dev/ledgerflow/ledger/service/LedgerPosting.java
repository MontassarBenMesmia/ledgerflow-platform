package dev.ledgerflow.ledger.service;

import dev.ledgerflow.ledger.domain.LedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class LedgerPosting {
    private LedgerPosting() {}

    public static List<LedgerEntry> balancedTransfer(UUID transferId, UUID sourceAccountId,
                                                      UUID destinationAccountId, BigDecimal amount,
                                                      String currency, Instant createdAt) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        return List.of(
                new LedgerEntry(UUID.randomUUID(), transferId, sourceAccountId,
                        LedgerEntry.Direction.DEBIT, amount, currency, createdAt),
                new LedgerEntry(UUID.randomUUID(), transferId, destinationAccountId,
                        LedgerEntry.Direction.CREDIT, amount, currency, createdAt));
    }

    public static BigDecimal signedTotal(List<LedgerEntry> entries) {
        return entries.stream()
                .map(entry -> entry.getDirection() == LedgerEntry.Direction.DEBIT
                        ? entry.getAmount().negate() : entry.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
