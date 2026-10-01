package dev.ledgerflow.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {
    public enum Status { COMPLETED }

    @Id
    private UUID id;
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;
    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;
    @Column(name = "destination_account_id", nullable = false)
    private UUID destinationAccountId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(name = "initiated_by", nullable = false)
    private String initiatedBy;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transfer() {}

    public Transfer(UUID id, String idempotencyKey, UUID sourceAccountId, UUID destinationAccountId,
                    BigDecimal amount, String currency, String initiatedBy, Instant createdAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.status = Status.COMPLETED;
        this.initiatedBy = initiatedBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getDestinationAccountId() { return destinationAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Status getStatus() { return status; }
    public String getInitiatedBy() { return initiatedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
