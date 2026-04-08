package com.digitalearn.npaxis.subscription.entity;

import com.digitalearn.npaxis.auditing.BaseEntity;
import com.digitalearn.npaxis.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "subscriptions")
public class SubscriptionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "stripe_subscription_id", nullable = false, unique = true)
    private String stripeSubscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_price_id", nullable = false)
    private PlanPrice planPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;

    @Builder.Default
    private boolean cancelAtPeriodEnd = false;
}
