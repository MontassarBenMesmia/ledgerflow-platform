package dev.ledgerflow.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance;
    @Version
    private long version;

    protected Account() {}

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public long getVersion() { return version; }
    public void debit(BigDecimal amount) { currentBalance = currentBalance.subtract(amount); }
    public void credit(BigDecimal amount) { currentBalance = currentBalance.add(amount); }
}
