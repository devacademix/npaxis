package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    Optional<InvoiceEntity> findByStripeInvoiceId(String stripeInvoiceId);
}
