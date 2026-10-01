package dev.ledgerflow.projection.repository;

import dev.ledgerflow.projection.domain.TransferView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferViewRepository extends JpaRepository<TransferView, UUID> {
    List<TransferView> findAllByOrderByOccurredAtDesc(Pageable pageable);
    long countByReconciliationStatus(String reconciliationStatus);
}
