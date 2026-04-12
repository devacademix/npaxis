package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.auditing.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeWebhookEventRepository extends BaseRepository<StripeWebhookEvent, Long> {
    boolean existsByEventId(String eventId);
}