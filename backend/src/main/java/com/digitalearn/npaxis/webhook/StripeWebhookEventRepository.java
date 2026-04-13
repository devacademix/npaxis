package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripeWebhookEventRepository extends BaseRepository<StripeWebhookEvent, Long> {

    boolean existsByEventId(String eventId);

    Optional<StripeWebhookEvent> findByEventId(String eventId);
}