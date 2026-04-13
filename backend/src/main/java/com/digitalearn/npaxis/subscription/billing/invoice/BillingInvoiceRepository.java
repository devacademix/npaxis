package com.digitalearn.npaxis.subscription.billing.invoice;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingInvoiceRepository extends BaseRepository<BillingInvoice, Long> {

    Optional<BillingInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    Page<BillingInvoice> findByPreceptor_UserIdOrderByInvoiceCreatedAtDesc(Long preceptorId, Pageable pageable);
}

