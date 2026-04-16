package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
}

