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
import jakarta.persistence.OneToOne;
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
                @Index(name = "idx_preceptor_subscription_preceptor", columnList = "preceptor_id"),
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

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preceptor_id", nullable = false, unique = true)
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
}