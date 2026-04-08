package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Optional<WebhookEvent> findByStripeEventId(String stripeEventId);
}
