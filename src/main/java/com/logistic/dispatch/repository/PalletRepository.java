package com.logistic.dispatch.repository;

import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.utility.LifeCycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PalletRepository extends JpaRepository<Pallet, UUID> {

    List<Pallet> findByProductId(UUID productId);

    List<Pallet> findByProductIdAndStatus(UUID productId, LifeCycleStatus status);

    List<Pallet> findByStatus(LifeCycleStatus status);

    long countByProductIdAndCreatedAtBetween(UUID productId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Pallet p WHERE p.productId = :productId AND p.status = 'CLOSED' AND p.closedAt BETWEEN :start AND :end")
    Long countClosedPalletsByDate(UUID productId, LocalDateTime start, LocalDateTime end);

    Long countByProductIdAndStatus(UUID productId, LifeCycleStatus status);

    Optional<Pallet> findByPalletSerialNumber(String palletSerialNumber);

    Page<Pallet> findByStatus(LifeCycleStatus status, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Pallet p WHERE p.productId = :productId AND p.status = 'OPEN' AND p.createdAt BETWEEN :start AND :end")
    Long countOpenPalletsByDate(UUID productId, LocalDateTime start, LocalDateTime end);

    List<Pallet> findByProductIdAndCreatedAtBetween(UUID productId, LocalDateTime start, LocalDateTime end);
}
