package dev.ledgerflow.projection.domain;

import dev.ledgerflow.contracts.TransferCompletedEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer_views")
public class TransferView {
    @Id
    private UUID id;
    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;
    @Column(name = "destination_account_id", nullable = false)
    private UUID destinationAccountId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "initiated_by", nullable = false)
    private String initiatedBy;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    @Column(name = "reconciliation_status", nullable = false)
    private String reconciliationStatus;

    protected TransferView() {}

    public TransferView(TransferCompletedEvent event, String reconciliationStatus) {
        id = event.transferId();
        sourceAccountId = event.sourceAccountId();
        destinationAccountId = event.destinationAccountId();
        amount = event.amount();
        currency = event.currency();
        initiatedBy = event.initiatedBy();
        occurredAt = event.occurredAt();
        this.reconciliationStatus = reconciliationStatus;
    }

    public UUID getId() { return id; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getDestinationAccountId() { return destinationAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getInitiatedBy() { return initiatedBy; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getReconciliationStatus() { return reconciliationStatus; }
}
