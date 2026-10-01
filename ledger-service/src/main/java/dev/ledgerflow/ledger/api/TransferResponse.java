package dev.ledgerflow.ledger.api;

import dev.ledgerflow.ledger.domain.Transfer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(UUID transferId, String status, UUID sourceAccountId,
                               UUID destinationAccountId, BigDecimal amount,
                               String currency, Instant createdAt) {
    static TransferResponse from(Transfer transfer) {
        return new TransferResponse(transfer.getId(), transfer.getStatus().name(),
                transfer.getSourceAccountId(), transfer.getDestinationAccountId(),
                transfer.getAmount(), transfer.getCurrency(), transfer.getCreatedAt());
    }
}
