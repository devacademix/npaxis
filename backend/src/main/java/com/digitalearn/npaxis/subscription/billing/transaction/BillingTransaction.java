package com.digitalearn.npaxis.subscription.billing.transaction;

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

@Entity
@Table(
        name = "billing_transactions",
        indexes = {
                @Index(name = "idx_tx_preceptor", columnList = "preceptor_id"),
                @Index(name = "idx_tx_stripe_payment_intent", columnList = "stripe_payment_intent_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BillingTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preceptor_id", nullable = false)
    private Preceptor preceptor;

    @Column(length = 120, unique = true)
    private String stripePaymentIntentId;

    @Column(length = 120)
    private String stripeInvoiceId;

    @Column(length = 120)
    private String stripeSubscriptionId;

    @Column(nullable = false)
    private Long amountInMinorUnits;

    @Column(length = 10, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status; // SUCCEEDED, FAILED, PENDING, REFUNDED

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime transactionAt;
}