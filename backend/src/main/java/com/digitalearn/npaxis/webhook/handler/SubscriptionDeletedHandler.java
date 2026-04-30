package com.digitalearn.npaxis.webhook.handler;

import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
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
 * Webhook handler for customer.subscription.deleted events.
 * Disables premium access and marks subscription as canceled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionDeletedHandler implements WebhookEventHandler {

    private final PreceptorRepository preceptorRepository;
    private final SubscriptionService subscriptionService;

    @Override
    public String eventType() {
        return "customer.subscription.deleted";
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Event event, WebhookProcessingEvent webhookRecord) {
        log.info("Handling customer.subscription.deleted event: {}", event.getId());

        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Subscription subscription = (Subscription) deserializer.deserializeUnsafe();

            if (subscription == null) {
                throw new IllegalStateException("Failed to deserialize Subscription object from event");
            }

            // Sync subscription state from Stripe (will map status to CANCELED)
            subscriptionService.syncLocalSubscriptionFromStripe(subscription.getId());

            // Disable premium access
            String customerId = subscription.getCustomer();
            Preceptor preceptor = preceptorRepository.findByStripeCustomerId(customerId)
                    .orElseThrow(() -> new IllegalStateException("Preceptor not found for customer: " + customerId));

            preceptor.setPremium(false);
            preceptorRepository.save(preceptor);

            webhookRecord.markSucceeded();
            log.info("Subscription deleted successfully: {}", subscription.getId());

        } catch (Exception e) {
            log.error("Error handling customer.subscription.deleted: {}", event.getId(), e);
            webhookRecord.markFailed(e.getMessage());
            webhookRecord.incrementRetry();
        }
    }
}


