package dev.ledgerflow.ledger.repository;

import dev.ledgerflow.ledger.domain.Transfer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
    List<Transfer> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
