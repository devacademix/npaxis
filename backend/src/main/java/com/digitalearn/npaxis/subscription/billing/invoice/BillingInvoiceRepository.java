package com.digitalearn.npaxis.subscription.billing.invoice;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingInvoiceRepository extends BaseRepository<BillingInvoice, Long> {
}