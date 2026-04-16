package com.digitalearn.npaxis.subscription.billing.invoice;

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
        name = "billing_invoices",
        indexes = @Index(name = "idx_billing_invoice_preceptor", columnList = "preceptor_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BillingInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preceptor_id", nullable = false)
    private Preceptor preceptor;

    @Column(name = "stripe_invoice_id", nullable = false, unique = true, length = 120)
    private String stripeInvoiceId;

    @Column(length = 120)
    private String stripeSubscriptionId;

    @Column(length = 120)
    private String stripeCustomerId;

    @Column(nullable = false)
    private Long amountPaidInMinorUnits;

    @Column(nullable = false)
    private Long amountDueInMinorUnits;

    @Column(length = 10, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status; // DRAFT, OPEN, PAID, VOID, UNCOLLECTIBLE

    @Column(length = 500)
    private String hostedInvoiceUrl;

    @Column(length = 500)
    private String invoicePdfUrl;

    @Column(name = "invoice_created_at")
    private LocalDateTime invoiceCreatedAt;

    @Column(name = "invoice_paid_at")
    private LocalDateTime invoicePaidAt;
}