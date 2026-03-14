package com.logistic.dispatch.repository;

import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.utility.LifeCycleStatus;
import com.logistic.dispatch.utility.QrStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    Optional<Batch> findByBatchSerialNumber(String batchSerialNumber);

    Optional<Batch> findByProductIdAndStatus(Long productId, LifeCycleStatus status);

    List<Batch> findByStatus(LifeCycleStatus status);

    Optional<Batch> findByProductIdAndStatus(UUID productId, LifeCycleStatus status);

    List<Batch> findByQrStatus(QrStatus qrStatus);

    @Query("SELECT COUNT(b) FROM Batch b WHERE b.productId = :productId AND b.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countTodayBatches(UUID productId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT COUNT(b) FROM Batch b WHERE b.productId = :productId AND b.status = 'CLOSED' AND b.closedAt BETWEEN :start AND :end")
    Long countClosedBatchesByDate(UUID productId, LocalDateTime start, LocalDateTime end);

    @Query(" SELECT SUM(b.currentUnits) FROM Batch b WHERE b.productId = :productId AND b.status = 'CLOSED' AND b.closedAt BETWEEN :start AND :end")
    Long sumClosedSerialsByDate(UUID productId, LocalDateTime start, LocalDateTime end);

    Long countByProductIdAndStatus(UUID productId, LifeCycleStatus status);

    @Query("SELECT b FROM Batch b WHERE b.productId = :productId AND b.status = 'CLOSED' AND b.closedAt BETWEEN :start AND :end ORDER BY b.closedAt DESC")
    List<Batch> findClosedBatchesByDate(UUID productId, LocalDateTime start, LocalDateTime end);

}
