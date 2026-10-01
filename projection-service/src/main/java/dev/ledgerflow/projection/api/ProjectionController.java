package dev.ledgerflow.projection.api;

import dev.ledgerflow.projection.domain.TransferView;
import dev.ledgerflow.projection.repository.ProcessedEventRepository;
import dev.ledgerflow.projection.repository.TransferViewRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProjectionController {
    private final TransferViewRepository transfers;
    private final ProcessedEventRepository processedEvents;

    public ProjectionController(TransferViewRepository transfers, ProcessedEventRepository processedEvents) {
        this.transfers = transfers;
        this.processedEvents = processedEvents;
    }

    @GetMapping("/activity")
    public List<TransferView> activity(@RequestParam(defaultValue = "20") int limit) {
        return transfers.findAllByOrderByOccurredAtDesc(PageRequest.of(0, Math.min(limit, 100)));
    }

    @GetMapping("/reconciliation")
    public Map<String, Object> reconciliation() {
        long total = transfers.count();
        long reviewRequired = transfers.countByReconciliationStatus("REVIEW_REQUIRED");
        return Map.of(
                "status", reviewRequired == 0 ? "HEALTHY" : "ATTENTION_REQUIRED",
                "processedEvents", processedEvents.count(),
                "projectedTransfers", total,
                "balancedTransfers", total - reviewRequired,
                "reviewRequired", reviewRequired,
                "checkedAt", Instant.now());
    }
}
