package com.digitalearn.npaxis.subscription.billing.invoice;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BillingInvoiceRepository extends BaseRepository<BillingInvoice, Long> {

    Optional<BillingInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    Page<BillingInvoice> findByPreceptor_UserIdOrderByInvoiceCreatedAtDesc(Long preceptorId, Pageable pageable);

    /**
     * UPSERT operation for invoices - PostgreSQL ON CONFLICT DO UPDATE
     * Handles concurrent webhook events safely (idempotent)
     * Only unique field: stripe_invoice_id
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO billing_invoices (
                preceptor_id,
                stripe_invoice_id,
                stripe_customer_id,
                stripe_subscription_id,
                amount_paid_in_minor_units,
                amount_due_in_minor_units,
                currency,
                status,
                hosted_invoice_url,
                invoice_pdf_url,
                invoice_created_at,
                invoice_paid_at,
                created_at,
                last_modified_at,
                deleted,
                deleted_at,
                created_by,
                last_modified_by,
                deleted_by
            )
            VALUES (
                :preceptorId,
                :stripeInvoiceId,
                :stripeCustomerId,
                :stripeSubscriptionId,
                :amountPaid,
                :amountDue,
                :currency,
                CAST(:status AS VARCHAR),
                :hostedInvoiceUrl,
                :invoicePdfUrl,
                :invoiceCreatedAt,
                :invoicePaidAt,
                NOW(),
                NOW(),
                false,
                NULL,
                NULL,
                NULL,
                NULL
            )
            ON CONFLICT (stripe_invoice_id)
            DO UPDATE SET
                stripe_customer_id = EXCLUDED.stripe_customer_id,
                stripe_subscription_id = EXCLUDED.stripe_subscription_id,
                amount_paid_in_minor_units = EXCLUDED.amount_paid_in_minor_units,
                amount_due_in_minor_units = EXCLUDED.amount_due_in_minor_units,
                currency = EXCLUDED.currency,
                status = EXCLUDED.status,
                hosted_invoice_url = EXCLUDED.hosted_invoice_url,
                invoice_pdf_url = EXCLUDED.invoice_pdf_url,
                invoice_created_at = EXCLUDED.invoice_created_at,
                invoice_paid_at = EXCLUDED.invoice_paid_at,
                last_modified_at = NOW()
            """, nativeQuery = true)
    void upsertInvoice(
            @Param("preceptorId") Long preceptorId,
            @Param("stripeInvoiceId") String stripeInvoiceId,
            @Param("stripeCustomerId") String stripeCustomerId,
            @Param("stripeSubscriptionId") String stripeSubscriptionId,
            @Param("amountPaid") Long amountPaid,
            @Param("amountDue") Long amountDue,
            @Param("currency") String currency,
            @Param("status") String status,
            @Param("hostedInvoiceUrl") String hostedInvoiceUrl,
            @Param("invoicePdfUrl") String invoicePdfUrl,
            @Param("invoiceCreatedAt") LocalDateTime invoiceCreatedAt,
            @Param("invoicePaidAt") LocalDateTime invoicePaidAt
    );

    @Query("""
    SELECT COUNT(bi)
    FROM BillingInvoice bi
    WHERE bi.status = com.digitalearn.npaxis.subscription.billing.invoice.InvoiceStatus.PAID
      AND (bi.deleted = false OR bi.deleted IS NULL)
    """)
    long countPaidInvoices();

    @Query("""
    SELECT COALESCE(SUM(bi.amountPaidInMinorUnits), 0)
    FROM BillingInvoice bi
    WHERE bi.status = com.digitalearn.npaxis.subscription.billing.invoice.InvoiceStatus.PAID
      AND bi.invoicePaidAt >= :start
      AND bi.invoicePaidAt < :end
      AND (bi.deleted = false OR bi.deleted IS NULL)
    """)
    long getRevenueBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT COALESCE(SUM(bi.amountPaidInMinorUnits), 0)
    FROM BillingInvoice bi
    WHERE bi.status = com.digitalearn.npaxis.subscription.billing.invoice.InvoiceStatus.PAID
      AND (bi.deleted = false OR bi.deleted IS NULL)
    """)
    long getTotalRevenueInMinorUnits();

    Page<BillingInvoice> findAllByOrderByInvoicePaidAtDesc(Pageable pageable);

    @Query("SELECT MIN(bi.invoiceCreatedAt) FROM BillingInvoice bi WHERE bi.status = 'PAID'")
    java.time.LocalDateTime getFirstInvoiceDate();
}
