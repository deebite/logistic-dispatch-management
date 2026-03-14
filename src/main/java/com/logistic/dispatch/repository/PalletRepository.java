package com.logistic.dispatch.repository;

import com.logistic.dispatch.entitiy.Pallet;
import com.logistic.dispatch.utility.LifeCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PalletRepository extends JpaRepository<Pallet, UUID> {

    Optional<Pallet> findByPalletId(UUID palletId);

    List<Pallet> findByProductIdAndStatus(UUID productId, LifeCycleStatus status);

    List<Pallet> findByStatus(LifeCycleStatus status);

    long countByProductIdAndCreatedAtBetween(UUID productId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Pallet p WHERE p.productId = :productId AND p.status = 'CLOSED' AND p.closedAt BETWEEN :start AND :end")
    Long countClosedPalletsByDate(UUID productId, LocalDateTime start, LocalDateTime end);

    Long countByProductIdAndStatus(UUID productId, LifeCycleStatus status);

}
