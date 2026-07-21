package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.notification.AlertDeliveryStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface AlertDeliverySpringDataRepository extends JpaRepository<AlertDeliveryJpaEntity, UUID> {

    /**
     * Due deliveries, highest urgency first, locked with SKIP LOCKED so several
     * application instances can drain the same queue concurrently without ever
     * sending the same alert twice.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            SELECT d FROM AlertDeliveryJpaEntity d
            WHERE d.status = com.laboussole.domain.model.notification.AlertDeliveryStatus.PENDING
              AND d.nextAttemptAt <= :now
            ORDER BY d.dispatchPriority ASC, d.nextAttemptAt ASC
            """)
    List<AlertDeliveryJpaEntity> lockDueForDispatch(@Param("now") Instant now, Pageable pageable);

    List<AlertDeliveryJpaEntity> findByStatusAndUpdatedAtBeforeOrderByUpdatedAtAsc(
            AlertDeliveryStatus status, Instant updatedBefore, Pageable pageable);

    List<AlertDeliveryJpaEntity> findByEventIdOrderByDispatchPriorityAscCreatedAtDesc(UUID eventId);

    long countByStatus(AlertDeliveryStatus status);
}
