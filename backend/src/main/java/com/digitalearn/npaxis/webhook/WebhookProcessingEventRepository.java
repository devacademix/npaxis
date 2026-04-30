package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookProcessingEventRepository extends BaseRepository<WebhookProcessingEvent, Long> {

    /**
     * Check if event with given ID has been processed
     */
    boolean existsByEventId(String eventId);

    /**
     * Find event by Stripe event ID
     */
    Optional<WebhookProcessingEvent> findByEventId(String eventId);

    /**
     * Find all failed events for retry
     */
    List<WebhookProcessingEvent> findByStatusAndNextRetryAtBefore(
            WebhookEventStatus status,
            LocalDateTime now
    );

    /**
     * Find all pending events
     */
    List<WebhookProcessingEvent> findByStatus(WebhookEventStatus status);

    /**
     * Get event history with pagination
     */
    Page<WebhookProcessingEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);


    // ========================
    // 🆕 IDEMPOTENCY & DUPLICATES
    // ========================

    Optional<WebhookProcessingEvent> findByEventIdAndStatus(
            String eventId,
            WebhookEventStatus status
    );

    boolean existsByEventIdAndStatus(
            String eventId,
            WebhookEventStatus status
    );


    // ========================
    // 🆕 RETRY IMPROVEMENTS (PAGINATED)
    // ========================

    Page<WebhookProcessingEvent> findByStatusAndNextRetryAtBeforeOrderByNextRetryAtAsc(
            WebhookEventStatus status,
            LocalDateTime now,
            Pageable pageable
    );


    // ========================
    // 🆕 METRICS / MONITORING
    // ========================

    long countByStatus(WebhookEventStatus status);


    // ========================
    // 🆕 DEBUGGING / OPERATIONS
    // ========================

    @Query("""
                SELECT w FROM WebhookProcessingEvent w
                WHERE w.status IN :statuses
                ORDER BY w.createdAt ASC
            """)
    List<WebhookProcessingEvent> findOldestByStatuses(
            @Param("statuses") List<WebhookEventStatus> statuses,
            Pageable pageable
    );


    // ========================
    // 🆕 DATE RANGE FILTERING
    // ========================

    Page<WebhookProcessingEvent> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );


    // ========================
    // 🆕 BULK RETRY FETCH (SCHEDULER FRIENDLY)
    // ========================

    @Query("""
                SELECT w FROM WebhookProcessingEvent w
                WHERE w.status = :status
                AND (w.nextRetryAt IS NULL OR w.nextRetryAt <= :now)
                ORDER BY w.nextRetryAt ASC
            """)
    List<WebhookProcessingEvent> findReadyForRetry(
            @Param("status") WebhookEventStatus status,
            @Param("now") LocalDateTime now
    );

}

