package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.auditing.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Audit trail for subscription state changes
 * Tracks all lifecycle events: creation, activation, cancellation, updates, etc.
 * <p>
 * This enables:
 * - Full audit logs
 * - Debugging subscription issues
 * - Analytics on subscription patterns
 * - Compliance and regulatory reporting
 */
@Entity
@Table(
        name = "subscription_events",
        indexes = {
                @Index(name = "idx_subscription_events_subscription", columnList = "preceptor_subscription_id"),
                @Index(name = "idx_subscription_events_preceptor", columnList = "preceptor_id,created_at DESC"),
                @Index(name = "idx_subscription_events_stripe", columnList = "stripe_event_id"),
                @Index(name = "idx_subscription_events_type", columnList = "event_type,created_at DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SubscriptionEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionEventId;

    /**
     * Foreign key to the subscription this event relates to
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preceptor_subscription_id", nullable = false)
    private PreceptorSubscription subscription;

    /**
     * Denormalized preceptor ID for efficient queries on user event history
     * Redundant but improves query performance without JOIN
     */
    @Column(name = "preceptor_id", nullable = false)
    private Long preceptorId;

    /**
     * Type of event that occurred
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 50, nullable = false)
    private SubscriptionEventType eventType;

    /**
     * Webhook event ID from Stripe (if applicable)
     * Used to prevent duplicate processing of the same Stripe event
     */
    @Column(name = "stripe_event_id", length = 255)
    private String stripeEventId;

    /**
     * Detailed information about the event
     * Could include old/new values, reasons, error messages, etc.
     * Stored as JSON for flexibility
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private java.util.Map<String, Object> details;

    /**
     * Event status (for async/failed processing)
     * PENDING: event created but not processed
     * SUCCESS: event processed successfully
     * FAILED: event processing failed
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SubscriptionEventStatus status = SubscriptionEventStatus.SUCCESS;

    /**
     * Error message if processing failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}



