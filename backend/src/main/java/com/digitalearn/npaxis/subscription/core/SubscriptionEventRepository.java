package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for subscription events audit trail
 */
@Repository
public interface SubscriptionEventRepository extends BaseRepository<SubscriptionEvent, Long> {

    /**
     * Get all events for a subscription ordered by creation date (newest first)
     */
    Page<SubscriptionEvent> findBySubscription_PreceptorSubscriptionIdOrderByCreatedAtDesc(
            Long subscriptionId,
            Pageable pageable
    );

    /**
     * Get all events for a preceptor ordered by creation date (newest first)
     * Used to view the full audit trail of a user's subscription history
     */
    Page<SubscriptionEvent> findByPreceptorIdOrderByCreatedAtDesc(
            Long preceptorId,
            Pageable pageable
    );

    /**
     * Get all events of a specific type for a preceptor
     */
    List<SubscriptionEvent> findByPreceptorIdAndEventTypeOrderByCreatedAtDesc(
            Long preceptorId,
            SubscriptionEventType eventType
    );

    /**
     * Get events within a date range for analytics
     */
    List<SubscriptionEvent> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Get events of specific types within a date range
     */
    List<SubscriptionEvent> findByEventTypeInAndCreatedAtBetweenOrderByCreatedAtDesc(
            List<SubscriptionEventType> eventTypes,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Check if an event for a Stripe event ID already exists (idempotency)
     */
    Optional<SubscriptionEvent> findByStripeEventId(String stripeEventId);

    /**
     * Count events by type for a subscription
     */
    long countBySubscription_PreceptorSubscriptionIdAndEventType(
            Long subscriptionId,
            SubscriptionEventType eventType
    );

    /**
     * Find failed events for retry
     */
    List<SubscriptionEvent> findByStatusAndCreatedAtBefore(
            SubscriptionEventStatus status,
            LocalDateTime beforeDate
    );

    /**
     * Get the most recent specific event for a subscription
     */
    Optional<SubscriptionEvent> findFirstBySubscription_PreceptorSubscriptionIdAndEventTypeOrderByCreatedAtDesc(
            Long subscriptionId,
            SubscriptionEventType eventType
    );

    /**
     * Get event count for subscriptions created in a time period (analytics)
     */
    @Query("""
            SELECT COUNT(DISTINCT se.subscription.preceptorSubscriptionId)
            FROM SubscriptionEvent se
            WHERE se.eventType = 'CREATED'
            AND se.createdAt BETWEEN :startDate AND :endDate
            """)
    long countNewSubscriptionsInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

