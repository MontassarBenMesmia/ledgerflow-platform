package dev.ledgerflow.projection.service;

import dev.ledgerflow.contracts.TransferCompletedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationPolicyTest {
    @Test
    void acceptsBalancedTransfer() {
        TransferCompletedEvent event = event("42.50", "42.50", "42.50", UUID.randomUUID(), UUID.randomUUID());
        assertThat(ReconciliationPolicy.evaluate(event)).isEqualTo("BALANCED");
    }

    @Test
    void flagsMismatchedPosting() {
        TransferCompletedEvent event = event("42.50", "42.50", "40.00", UUID.randomUUID(), UUID.randomUUID());
        assertThat(ReconciliationPolicy.evaluate(event)).isEqualTo("REVIEW_REQUIRED");
    }

    @Test
    void flagsSelfTransfer() {
        UUID account = UUID.randomUUID();
        TransferCompletedEvent event = event("42.50", "42.50", "42.50", account, account);
        assertThat(ReconciliationPolicy.evaluate(event)).isEqualTo("REVIEW_REQUIRED");
    }

    private static TransferCompletedEvent event(String amount, String debit, String credit,
                                                 UUID source, UUID destination) {
        return new TransferCompletedEvent(UUID.randomUUID(), UUID.randomUUID(), source, destination,
                new BigDecimal(amount), "EUR", new BigDecimal(debit), new BigDecimal(credit),
                "test-user", Instant.parse("2026-01-01T00:00:00Z"));
    }
}
