package com.digitalearn.npaxis.subscription.core;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.subscription.plan.SubscriptionPlan;
import com.digitalearn.npaxis.subscription.price.SubscriptionPrice;
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

@Entity
@Table(
        name = "preceptor_subscriptions",
        indexes = {
                @Index(name = "idx_preceptor_subscription_active", columnList = "preceptor_id,is_active"),
                @Index(name = "idx_preceptor_subscription_history", columnList = "preceptor_id,created_at DESC"),
                @Index(name = "idx_preceptor_subscription_status_active", columnList = "status,is_active"),
                @Index(name = "idx_preceptor_subscription_stripe_sub", columnList = "stripe_subscription_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PreceptorSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long preceptorSubscriptionId;

    /**
     * Many-to-one relationship: Multiple subscriptions per preceptor (historical tracking)
     * This replaced the one-to-one relationship to support subscription history
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preceptor_id", nullable = false)
    private Preceptor preceptor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id", nullable = false)
    private SubscriptionPrice price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status; // TRIALING, ACTIVE, PAST_DUE, CANCELED, INCOMPLETE

    @Column(name = "stripe_customer_id", length = 120)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 120, unique = true)
    private String stripeSubscriptionId;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "cancel_at_period_end")
    private boolean cancelAtPeriodEnd;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "access_enabled", nullable = false)
    private boolean accessEnabled = false;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "canceled_reason", length = 255)
    private String canceledReason;

    @Column(columnDefinition = "TEXT")
    private String lastPaymentFailureReason;

    @Column(name = "payment_retry_count")
    private Integer paymentRetryCount = 0;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "is_cancelled", nullable = false)
    private boolean cancelled = false;

    /**
     * Start date of the subscription lifecycle
     * Represents when this particular subscription instance began
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * End date of the subscription lifecycle
     * Represents when this subscription ends (could be due to cancellation, expiration, etc.)
     * NULL for active subscriptions
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Date when subscription is scheduled to be cancelled at period end
     * Different from cancelled_at - this is future-scheduled cancellation
     */
    @Column(name = "cancel_date")
    private LocalDateTime cancelDate;

    /**
     * Flag indicating if this is an active subscription
     * Application-level enforcement of "one active subscription per preceptor"
     * When subscribing again, old subscriptions are marked active = false
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}