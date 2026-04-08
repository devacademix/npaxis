package com.digitalearn.npaxis.subscription.entity;

import com.digitalearn.npaxis.auditing.BaseEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Invoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "stripe_invoice_id", nullable = false, unique = true, length = 100)
    private String stripeInvoiceId;

    @Column(name = "amount_due", nullable = false)
    private BigDecimal amountDue;

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "invoice_pdf", length = 500)
    private String invoicePdf;

    @Column(name = "hosted_invoice_url", length = 500)
    private String hostedInvoiceUrl;
}
