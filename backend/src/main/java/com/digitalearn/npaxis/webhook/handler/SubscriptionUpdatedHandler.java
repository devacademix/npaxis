package com.digitalearn.npaxis.webhook.handler;

import com.digitalearn.npaxis.subscription.core.SubscriptionService;
import com.digitalearn.npaxis.webhook.WebhookProcessingEvent;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook handler for customer.subscription.updated events.
 * Syncs subscription state changes from Stripe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionUpdatedHandler implements WebhookEventHandler {

    private final SubscriptionService subscriptionService;

    @Override
    public String eventType() {
        return "customer.subscription.updated";
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Event event, WebhookProcessingEvent webhookRecord) {
        log.info("Handling customer.subscription.updated event: {}", event.getId());

        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Subscription subscription = (Subscription) deserializer.deserializeUnsafe();

            if (subscription == null) {
                throw new IllegalStateException("Failed to deserialize Subscription object from event");
            }

            // Sync subscription from Stripe
            subscriptionService.syncLocalSubscriptionFromStripe(subscription.getId());

            webhookRecord.markSucceeded();
            log.info("Subscription updated successfully: {}", subscription.getId());

        } catch (Exception e) {
            log.error("Error handling customer.subscription.updated: {}", event.getId(), e);
            webhookRecord.markFailed(e.getMessage());
            webhookRecord.incrementRetry();
        }
    }
}

