package dev.ledgerflow.ledger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.ledgerflow.ledger.domain.Account;
import dev.ledgerflow.ledger.domain.Transfer;
import dev.ledgerflow.ledger.repository.AccountRepository;
import dev.ledgerflow.ledger.repository.LedgerEntryRepository;
import dev.ledgerflow.ledger.repository.OutboxEventRepository;
import dev.ledgerflow.ledger.repository.TransferRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferServiceTest {
    private final AccountRepository accounts = mock(AccountRepository.class);
    private final TransferRepository transfers = mock(TransferRepository.class);
    private final LedgerEntryRepository entries = mock(LedgerEntryRepository.class);
    private final OutboxEventRepository outbox = mock(OutboxEventRepository.class);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Instant now = Instant.parse("2026-01-01T12:00:00Z");

    @Test
    void postsBalancedTransferAndUpdatesBalances() throws Exception {
        UUID sourceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID destinationId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Account source = account(sourceId, "Source", "100.00");
        Account destination = account(destinationId, "Destination", "20.00");
        when(transfers.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(accounts.findByIdForUpdate(sourceId)).thenReturn(Optional.of(source));
        when(accounts.findByIdForUpdate(destinationId)).thenReturn(Optional.of(destination));

        TransferService service = new TransferService(accounts, transfers, entries, outbox, mapper,
                Clock.fixed(now, ZoneOffset.UTC));
        Transfer result = service.create("key-1", sourceId, destinationId,
                new BigDecimal("25.00"), "eur", "demo");

        assertThat(result.getStatus()).isEqualTo(Transfer.Status.COMPLETED);
        assertThat(source.getCurrentBalance()).isEqualByComparingTo("75.00");
        assertThat(destination.getCurrentBalance()).isEqualByComparingTo("45.00");
        verify(entries).saveAll(any());
        verify(outbox).save(any());
    }

    @Test
    void returnsOriginalTransferForRepeatedIdempotencyKey() {
        Transfer existing = new Transfer(UUID.randomUUID(), "same-key", UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("12.00"), "EUR", "demo", now);
        when(transfers.findByIdempotencyKey("same-key")).thenReturn(Optional.of(existing));
        TransferService service = new TransferService(accounts, transfers, entries, outbox, mapper,
                Clock.fixed(now, ZoneOffset.UTC));

        Transfer result = service.create("same-key", existing.getSourceAccountId(),
                existing.getDestinationAccountId(), existing.getAmount(), "EUR", "demo");

        assertThat(result.getId()).isEqualTo(existing.getId());
        verify(accounts, never()).findByIdForUpdate(any());
    }

    private static Account account(UUID id, String name, String balance) throws Exception {
        var constructor = Account.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Account account = constructor.newInstance();
        set(account, "id", id);
        set(account, "name", name);
        set(account, "currency", "EUR");
        set(account, "currentBalance", new BigDecimal(balance));
        return account;
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
