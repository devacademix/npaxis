package com.digitalearn.npaxis.webhook.handler;

import com.digitalearn.npaxis.preceptor.Preceptor;
import com.digitalearn.npaxis.preceptor.PreceptorRepository;
import com.digitalearn.npaxis.webhook.WebhookProcessingEvent;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook handler for customer.updated events.
 * Syncs customer email and name changes back to preceptor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerUpdatedHandler implements WebhookEventHandler {

    private final PreceptorRepository preceptorRepository;

    @Override
    public String eventType() {
        return "customer.updated";
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(Event event, WebhookProcessingEvent webhookRecord) {
        log.info("Handling customer.updated event: {}", event.getId());

        try {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Customer customer = (Customer) deserializer.deserializeUnsafe();

            if (customer == null) {
                throw new IllegalStateException("Failed to deserialize Customer object from event");
            }

            // Find preceptor by Stripe customer ID
            Preceptor preceptor = preceptorRepository.findByStripeCustomerId(customer.getId())
                    .orElse(null);

            if (preceptor != null) {
                // Sync email and name if changed
                if (customer.getEmail() != null && !customer.getEmail().equals(preceptor.getEmail())) {
                    preceptor.setEmail(customer.getEmail());
                }
                if (customer.getName() != null && !customer.getName().equals(preceptor.getName())) {
                    preceptor.setName(customer.getName());
                }
                preceptorRepository.save(preceptor);
                log.debug("Customer details synced for preceptor: {}", preceptor.getUserId());
            }

            webhookRecord.markSucceeded();
            log.info("Customer updated successfully: {}", customer.getId());

        } catch (Exception e) {
            log.error("Error handling customer.updated: {}", event.getId(), e);
            webhookRecord.markFailed(e.getMessage());
            webhookRecord.incrementRetry();
        }
    }
}

