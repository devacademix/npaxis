package com.digitalearn.npaxis.webhook;

import com.stripe.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for processing Stripe webhook events
 */
public interface WebhookService {

    /**
     * Process a Stripe webhook event
     *
     * @param event   the Stripe event
     * @param payload the raw event payload for storage
     */
    void process(Event event, String payload);

    /**
     * Retry processing of failed webhook events
     * Scheduled to run periodically
     */
    void retryFailedEvents();

    /**
     * Get webhook event processing history
     *
     * @param pageable pagination details
     * @return paginated webhook events
     */
    Page<WebhookEventResponse> getEventHistory(Pageable pageable);
}


