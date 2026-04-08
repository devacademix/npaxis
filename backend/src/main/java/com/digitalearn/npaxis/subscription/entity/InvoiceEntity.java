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
@Table(name = "invoices")
public class InvoiceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stripe_invoice_id", nullable = false, unique = true)
    private String stripeInvoiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private SubscriptionEntity subscription;

    @Column(nullable = false)
    private BigDecimal amountDue;

    @Column(nullable = false)
    private BigDecimal amountPaid;

    @Column(nullable = false)
    private String status;

    private String invoicePdf;
    private String hostedInvoiceUrl;
}
