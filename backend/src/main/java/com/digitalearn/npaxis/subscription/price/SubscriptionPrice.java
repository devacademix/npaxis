package com.digitalearn.npaxis.subscription.price;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.subscription.billing.BillingInterval;
import com.digitalearn.npaxis.subscription.plan.SubscriptionPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "subscription_prices",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "billing_interval", "currency"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SubscriptionPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval", nullable = false, length = 20)
    private BillingInterval billingInterval; // MONTHLY, YEARLY

    @Column(nullable = false, length = 10)
    private String currency = "usd";

    @Column(nullable = false)
    private Long amountInMinorUnits; // cents/paise

    @Column(length = 120, nullable = false)
    private String stripeProductId;

    @Column(length = 120, nullable = false)
    private String stripePriceId;

    @Column(nullable = false)
    private boolean active = true;
}