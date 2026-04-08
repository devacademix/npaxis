package com.digitalearn.npaxis.subscription.repository;

import com.digitalearn.npaxis.subscription.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    Optional<WebhookEvent> findByStripeEventId(String stripeEventId);
}
