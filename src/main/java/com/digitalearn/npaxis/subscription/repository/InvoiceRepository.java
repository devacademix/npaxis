package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByStripeInvoiceId(String invoiceId);
}
