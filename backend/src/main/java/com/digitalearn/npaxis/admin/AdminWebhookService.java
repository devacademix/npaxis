package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.WebhookEventDetailDTO;
import com.digitalearn.npaxis.admin.dto.WebhookMetricsDTO;
import com.digitalearn.npaxis.webhook.WebhookEventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for admin webhook management operations
 */
public interface AdminWebhookService {

    /**
     * Get paginated webhook event history
     *
     * @param pageable pagination parameters
     * @return paginated webhook event responses
     */
    Page<WebhookEventResponse> getWebhookEventHistory(Pageable pageable);

    /**
     * Get detailed view of specific webhook event
     *
     * @param eventId the Stripe event ID
     * @return webhook event details
     */
    WebhookEventDetailDTO getWebhookEventDetail(String eventId);

    /**
     * Retry a single failed webhook event
     *
     * @param eventId the event ID to retry
     * @return success message with retry schedule
     */
    String retryWebhookEvent(String eventId);

    /**
     * Get webhook metrics and statistics
     *
     * @return webhook metrics including success rate and stats
     */
    WebhookMetricsDTO getWebhookMetrics();
}

