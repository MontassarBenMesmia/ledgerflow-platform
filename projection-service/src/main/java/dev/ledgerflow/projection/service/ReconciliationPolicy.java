package dev.ledgerflow.projection.service;

import dev.ledgerflow.contracts.TransferCompletedEvent;

import java.math.BigDecimal;

public final class ReconciliationPolicy {
    private ReconciliationPolicy() {}

    public static String evaluate(TransferCompletedEvent event) {
        boolean valid = event.amount().signum() > 0
                && event.debitAmount().compareTo(event.creditAmount()) == 0
                && event.amount().compareTo(event.debitAmount()) == 0
                && !event.sourceAccountId().equals(event.destinationAccountId());
        return valid ? "BALANCED" : "REVIEW_REQUIRED";
    }
}
