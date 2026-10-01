package dev.ledgerflow.ledger.service;

import dev.ledgerflow.ledger.domain.OutboxEvent;
import dev.ledgerflow.ledger.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Component
public class OutboxPublisher {
    private final OutboxEventRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final String topic;
    private final Clock clock = Clock.systemUTC();

    public OutboxPublisher(OutboxEventRepository outbox, KafkaTemplate<String, String> kafka,
                           @Value("${ledger.events.topic}") String topic) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${ledger.outbox.publish-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outbox.findByPublishedAtIsNullOrderByCreatedAt(PageRequest.of(0, 100));
        for (OutboxEvent event : pending) {
            kafka.send(topic, event.getAggregateId().toString(), event.getPayload()).join();
            event.markPublished(clock.instant());
        }
    }
}
