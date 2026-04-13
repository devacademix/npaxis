package com.digitalearn.npaxis.subscription.config;

import com.digitalearn.npaxis.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for subscription and webhook management.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduledTasks {

    private final WebhookService webhookService;

    /**
     * Retry failed webhook events every 5 minutes.
     * This helps ensure that transient failures don't leave the system in an inconsistent state.
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000) // 5 minutes, 1 minute initial delay
    public void retryFailedWebhooks() {
        try {
            log.debug("Starting retry of failed webhook events");
            webhookService.retryFailedEvents();
        } catch (Exception e) {
            log.error("Error during webhook retry task", e);
        }
    }
}

