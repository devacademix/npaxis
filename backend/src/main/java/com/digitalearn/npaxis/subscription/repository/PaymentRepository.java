package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByStripePaymentIntentId(String stripePaymentIntentId);
}
