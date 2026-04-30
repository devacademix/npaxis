package com.digitalearn.npaxis.webhook.handler;

import com.digitalearn.npaxis.webhook.WebhookProcessingEvent;
import com.stripe.model.Event;

/**
 * Interface for Stripe webhook event handlers.
 * Each handler is responsible for processing a specific Stripe event type.
 */
public interface WebhookEventHandler {

    /**
     * Returns the Stripe event type this handler processes.
     * e.g., "charge.succeeded", "invoice.paid", "customer.subscription.created"
     */
    String eventType();

    /**
     * Handles the Stripe event.
     * Must never throw an exception; instead mark the record as failed.
     *
     * @param event         the Stripe event
     * @param webhookRecord the webhook processing record for audit
     */
    void handle(Event event, WebhookProcessingEvent webhookRecord);
}
