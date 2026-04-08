package com.digitalearn.npaxis.subscription.entity;

import com.digitalearn.npaxis.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "plan_prices")
public class PlanPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "stripe_price_id", nullable = false, unique = true)
    private String stripePriceId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_interval", nullable = false)
    private PlanInterval interval;

    @Builder.Default
    private boolean active = true;
}
