package dev.ledgerflow.ledger.service;

import dev.ledgerflow.ledger.domain.LedgerEntry;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerPostingPropertiesTest {
    @Property(tries = 250)
    void everyTransferProducesBalancedEntries(
            @ForAll @LongRange(min = 1, max = 100_000_000) long amountInCents) {
        BigDecimal amount = BigDecimal.valueOf(amountInCents, 2);
        List<LedgerEntry> posting = LedgerPosting.balancedTransfer(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), amount, "EUR", Instant.now());

        assertThat(posting).hasSize(2);
        assertThat(LedgerPosting.signedTotal(posting)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(posting).allMatch(entry -> entry.getAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}
