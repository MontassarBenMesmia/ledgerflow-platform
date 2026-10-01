package dev.ledgerflow.projection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ledgerflow.contracts.TransferCompletedEvent;
import dev.ledgerflow.projection.domain.ProcessedEvent;
import dev.ledgerflow.projection.domain.TransferView;
import dev.ledgerflow.projection.repository.ProcessedEventRepository;
import dev.ledgerflow.projection.repository.TransferViewRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Component
public class TransferEventConsumer {
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEvents;
    private final TransferViewRepository transfers;
    private final Counter duplicateEvents;
    private final Counter processedEventCounter;
    private final Counter reconciliationFailures;
    private final Clock clock = Clock.systemUTC();

    public TransferEventConsumer(ObjectMapper objectMapper, ProcessedEventRepository processedEvents,
                                 TransferViewRepository transfers, MeterRegistry registry) {
        this.objectMapper = objectMapper;
        this.processedEvents = processedEvents;
        this.transfers = transfers;
        duplicateEvents = registry.counter("ledgerflow_projection_duplicate_events_total");
        processedEventCounter = registry.counter("ledgerflow_projection_processed_events_total");
        reconciliationFailures = registry.counter("ledgerflow_reconciliation_failures_total");
    }

    @KafkaListener(topics = "${ledger.events.topic}", groupId = "ledgerflow-projection")
    @Transactional
    public void consume(String payload) throws JsonProcessingException {
        TransferCompletedEvent event = objectMapper.readValue(payload, TransferCompletedEvent.class);
        if (processedEvents.existsById(event.eventId())) {
            duplicateEvents.increment();
            return;
        }
        String status = ReconciliationPolicy.evaluate(event);
        if (!"BALANCED".equals(status)) {
            reconciliationFailures.increment();
        }
        transfers.save(new TransferView(event, status));
        processedEvents.save(new ProcessedEvent(event.eventId(), clock.instant()));
        processedEventCounter.increment();
    }
}
