package com.logistic.dispatch.repository;

import com.logistic.dispatch.entitiy.Batch;
import com.logistic.dispatch.utility.LifeCycleStatus;
import com.logistic.dispatch.utility.QrStatus;
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
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    Optional<Batch> findByBatchSerialNumber(String batchSerialNumber);

    List<Batch> findByProductId(UUID productId);

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

    Page<Batch> findByStatus(LifeCycleStatus status, Pageable pageable);

    Optional<Batch> findByProductIdAndAssignedUserIdAndStatus(UUID productId, UUID assignedUserId, LifeCycleStatus status);

    Optional<Batch> findFirstByProductIdAndAssignedUserIdIsNullAndStatus(UUID productId, LifeCycleStatus status);

    List<Batch> findByProductIdAndStatusInOrderByCreatedAtAsc(UUID productId, List<LifeCycleStatus> statuses);

    List<Batch> findByAssignedUserIdAndStatus(UUID assignedUserId, LifeCycleStatus status);


    @Query("SELECT COUNT(b) FROM Batch b WHERE b.productId = :productId AND b.status IN ('AVAILABLE','IN_PROGRESS') AND b.createdAt BETWEEN :start AND :end")
    Long countInProgressBatches(UUID productId, LocalDateTime start, LocalDateTime end);

    List<Batch> findByProductIdAndCreatedAtBetween(UUID productId, LocalDateTime start, LocalDateTime end);
}
