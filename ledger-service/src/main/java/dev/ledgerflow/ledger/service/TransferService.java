package dev.ledgerflow.ledger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ledgerflow.contracts.TransferCompletedEvent;
import dev.ledgerflow.ledger.domain.Account;
import dev.ledgerflow.ledger.domain.LedgerEntry;
import dev.ledgerflow.ledger.domain.OutboxEvent;
import dev.ledgerflow.ledger.domain.Transfer;
import dev.ledgerflow.ledger.repository.AccountRepository;
import dev.ledgerflow.ledger.repository.LedgerEntryRepository;
import dev.ledgerflow.ledger.repository.OutboxEventRepository;
import dev.ledgerflow.ledger.repository.TransferRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TransferService {
    private final AccountRepository accounts;
    private final TransferRepository transfers;
    private final LedgerEntryRepository entries;
    private final OutboxEventRepository outbox;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public TransferService(AccountRepository accounts, TransferRepository transfers,
                           LedgerEntryRepository entries, OutboxEventRepository outbox,
                           ObjectMapper objectMapper) {
        this(accounts, transfers, entries, outbox, objectMapper, Clock.systemUTC());
    }

    TransferService(AccountRepository accounts, TransferRepository transfers,
                    LedgerEntryRepository entries, OutboxEventRepository outbox,
                    ObjectMapper objectMapper, Clock clock) {
        this.accounts = accounts;
        this.transfers = transfers;
        this.entries = entries;
        this.outbox = outbox;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public Transfer create(String idempotencyKey, UUID sourceId, UUID destinationId,
                           BigDecimal amount, String currency, String initiatedBy) {
        return transfers.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> postTransfer(idempotencyKey, sourceId, destinationId, amount, currency, initiatedBy));
    }

    private Transfer postTransfer(String idempotencyKey, UUID sourceId, UUID destinationId,
                                  BigDecimal amount, String currency, String initiatedBy) {
        if (sourceId.equals(destinationId)) {
            throw new LedgerException("Source and destination accounts must differ");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new LedgerException("Amount must be positive");
        }

        List<UUID> lockOrder = List.of(sourceId, destinationId).stream().sorted(Comparator.naturalOrder()).toList();
        Account first = lockAccount(lockOrder.get(0));
        Account second = lockAccount(lockOrder.get(1));
        Account source = first.getId().equals(sourceId) ? first : second;
        Account destination = first.getId().equals(destinationId) ? first : second;

        // A competing request with the same key may have completed while this one waited for locks.
        var concurrentResult = transfers.findByIdempotencyKey(idempotencyKey);
        if (concurrentResult.isPresent()) return concurrentResult.get();

        String normalizedCurrency = currency.toUpperCase();
        if (!source.getCurrency().equals(normalizedCurrency) || !destination.getCurrency().equals(normalizedCurrency)) {
            throw new LedgerException("Both accounts must use " + normalizedCurrency);
        }
        if (source.getCurrentBalance().compareTo(amount) < 0) {
            throw new LedgerException("Insufficient funds");
        }

        Instant now = clock.instant();
        UUID transferId = UUID.randomUUID();
        source.debit(amount);
        destination.credit(amount);

        Transfer transfer = new Transfer(transferId, idempotencyKey, sourceId, destinationId,
                amount, normalizedCurrency, initiatedBy, now);
        List<LedgerEntry> posting = LedgerPosting.balancedTransfer(
                transferId, sourceId, destinationId, amount, normalizedCurrency, now);
        if (LedgerPosting.signedTotal(posting).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Unbalanced ledger posting");
        }

        TransferCompletedEvent event = new TransferCompletedEvent(UUID.randomUUID(), transferId,
                sourceId, destinationId, amount, normalizedCurrency, amount, amount, initiatedBy, now);
        try {
            transfers.save(transfer);
            entries.saveAll(posting);
            outbox.save(new OutboxEvent(event.eventId(), transferId, "TransferCompleted",
                    objectMapper.writeValueAsString(event), now));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize transfer event", exception);
        }
        return transfer;
    }

    @Transactional(readOnly = true)
    public List<Transfer> recent(int limit) {
        return transfers.findAllByOrderByCreatedAtDesc(PageRequest.of(0, Math.min(limit, 100)));
    }

    private Account lockAccount(UUID id) {
        return accounts.findByIdForUpdate(id).orElseThrow(() -> new LedgerException("Account not found: " + id));
    }

    public static class LedgerException extends RuntimeException {
        public LedgerException(String message) { super(message); }
    }
}
