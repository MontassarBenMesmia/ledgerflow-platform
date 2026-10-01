package dev.ledgerflow.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompletedEvent(
        UUID eventId,
        UUID transferId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        String initiatedBy,
        Instant occurredAt) {
}
