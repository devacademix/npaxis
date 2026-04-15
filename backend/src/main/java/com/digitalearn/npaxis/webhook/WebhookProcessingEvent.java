package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.preceptor.Preceptor;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "webhook_processing_events",
        indexes = {
                @Index(name = "idx_webhook_event_id", columnList = "event_id"),
                @Index(name = "idx_webhook_event_type", columnList = "event_type"),
                @Index(name = "idx_webhook_status_retry", columnList = "status,retry_count")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WebhookProcessingEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stripe_event_id", nullable = false, unique = true, length = 120)
    private String eventId;

    @Column(nullable = false, length = 100)
    private String eventType; // e.g., "customer.subscription.created"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preceptor_id")
    private Preceptor preceptor;

    @Column(length = 120)
    private String stripeCustomerId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload; // Full webhook payload JSON

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookEventStatus status; // PENDING, PROCESSED, FAILED

    @Column(name = "processed_at")
    private java.time.LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage; // If status is FAILED

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(name = "next_retry_at")
    private java.time.LocalDateTime nextRetryAt;

    @Column(name = "livemode", nullable = false)
    private boolean liveMode = true;

    public void markSucceeded() {
        this.status = WebhookEventStatus.SUCCEEDED;
        this.processedAt = LocalDateTime.now(ZoneOffset.UTC);
        this.errorMessage = null;
    }

    public void markFailed(String message) {
        this.status = WebhookEventStatus.FAILED;
        this.errorMessage = message != null && message.length() > 1000
                ? message.substring(0, 1000)
                : message;
    }

    public void incrementRetry() {
        this.retryCount++;
    }
}

