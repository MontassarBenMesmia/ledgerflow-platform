package dev.ledgerflow.ledger.api;

import dev.ledgerflow.ledger.domain.Transfer;
import dev.ledgerflow.ledger.repository.AccountRepository;
import dev.ledgerflow.ledger.service.RateLimiter;
import dev.ledgerflow.ledger.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LedgerController {
    private final AccountRepository accounts;
    private final TransferService transfers;
    private final RateLimiter rateLimiter;

    public LedgerController(AccountRepository accounts, TransferService transfers, RateLimiter rateLimiter) {
        this.accounts = accounts;
        this.transfers = transfers;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/accounts")
    public List<AccountResponse> accounts() {
        return accounts.findAll().stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/transfers")
    public List<TransferResponse> transfers(@RequestParam(defaultValue = "20") int limit) {
        return transfers.recent(limit).stream().map(TransferResponse::from).toList();
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                     @Valid @RequestBody TransferRequest request,
                                     Authentication authentication) {
        rateLimiter.check(authentication.getName());
        Transfer result = transfers.create(idempotencyKey, request.sourceAccountId(),
                request.destinationAccountId(), request.amount(), request.currency(), authentication.getName());
        return TransferResponse.from(result);
    }
}
