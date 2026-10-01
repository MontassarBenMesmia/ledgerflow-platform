package dev.ledgerflow.projection.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private UUID id;
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {}
    public ProcessedEvent(UUID id, Instant processedAt) { this.id = id; this.processedAt = processedAt; }
    public UUID getId() { return id; }
    public Instant getProcessedAt() { return processedAt; }
}
