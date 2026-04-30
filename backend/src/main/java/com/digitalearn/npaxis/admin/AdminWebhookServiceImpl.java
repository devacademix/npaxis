package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.WebhookEventDetailDTO;
import com.digitalearn.npaxis.admin.dto.WebhookMetricsDTO;
import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import com.digitalearn.npaxis.webhook.WebhookEventResponse;
import com.digitalearn.npaxis.webhook.WebhookEventStatus;
import com.digitalearn.npaxis.webhook.WebhookProcessingEvent;
import com.digitalearn.npaxis.webhook.WebhookProcessingEventRepository;
import com.digitalearn.npaxis.webhook.WebhookService;
import com.digitalearn.npaxis.webhook.WebhookEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for admin webhook management
 * Provides endpoints for viewing and managing webhook events
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminWebhookServiceImpl implements AdminWebhookService {

    private final WebhookService webhookService;
    private final WebhookProcessingEventRepository webhookEventRepository;
    private final WebhookEventMapper webhookEventMapper;

    @Override
    public Page<WebhookEventResponse> getWebhookEventHistory(Pageable pageable) {
        log.info("Admin fetching webhook event history - page: {}, size: {}",
            pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<WebhookEventResponse> history = webhookService.getEventHistory(pageable);
            log.info("✓ Retrieved {} webhook events (total: {})",
                history.getContent().size(), history.getTotalElements());
            return history;

        } catch (Exception e) {
            log.error("Error fetching webhook event history: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    @Override
    public WebhookEventDetailDTO getWebhookEventDetail(String eventId) {
        log.info("Admin fetching webhook event detail - eventId: {}", eventId);

        try {
            WebhookProcessingEvent event = webhookEventRepository.findByEventId(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Webhook event not found with ID: " + eventId));

            String preceptorName = event.getPreceptor() != null ?
                    event.getPreceptor().getUser().getDisplayName() : "N/A";

            log.info("✓ Webhook event detail: id={}, type={}, status={}, preceptor={}",
                    eventId, event.getEventType(), event.getStatus(), preceptorName);

            return new WebhookEventDetailDTO(
                    event.getEventId(),
                    event.getEventType(),
                    event.getStatus().toString(),
                    event.getPayload(),
                    event.getRetryCount(),
                    event.getCreatedAt(),
                    event.getLastModifiedAt() != null ? event.getLastModifiedAt() : event.getCreatedAt()
            );

        } catch (ResourceNotFoundException e) {
            log.warn("Webhook event not found: {}", eventId);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching webhook event detail: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch webhook event details", e);
        }
    }

    @Override
    @Transactional
    public String retryWebhookEvent(String eventId) {
        log.info("Admin retrying webhook event - eventId: {}", eventId);

        try {
            WebhookProcessingEvent event = webhookEventRepository.findByEventId(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Webhook event not found with ID: " + eventId));

            // Verify event is in failed state
            if (!event.getStatus().equals(WebhookEventStatus.FAILED_RETRYING) &&
                !event.getStatus().equals(WebhookEventStatus.DEAD_LETTER)) {
                log.warn("Cannot retry event {} - current status: {}", eventId, event.getStatus());
                return "Event is not in a failed state. Current status: " + event.getStatus();
            }

            // Reset the event for retry
            event.setStatus(WebhookEventStatus.FAILED_RETRYING);
            event.setNextRetryAt(LocalDateTime.now()); // Retry immediately
            event.setErrorMessage(null);

            webhookEventRepository.save(event);

            // Trigger retry logic
            webhookService.retryFailedEvents();

            log.info("✓ Webhook event scheduled for retry: eventId={}, nextRetryAt={}",
                eventId, event.getNextRetryAt());

            return "Webhook event (ID: " + eventId + ") has been scheduled for retry. " +
                   "Check status in a few moments.";

        } catch (ResourceNotFoundException e) {
            log.warn("Webhook event not found for retry: {}", eventId);
            throw e;
        } catch (Exception e) {
            log.error("Error retrying webhook event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retry webhook event", e);
        }
    }

    @Override
    public WebhookMetricsDTO getWebhookMetrics() {
        log.info("Admin fetching webhook metrics");

        try {
            // Count events by status
            long successCount = webhookEventRepository.countByStatus(WebhookEventStatus.SUCCESS);
            long failedRetryingCount = webhookEventRepository.countByStatus(WebhookEventStatus.FAILED_RETRYING);
            long deadLetterCount = webhookEventRepository.countByStatus(WebhookEventStatus.DEAD_LETTER);

            // Calculate success rate
            long totalCount = successCount + failedRetryingCount + deadLetterCount;
            Double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0;

            // Calculate average retry count
            List<WebhookProcessingEvent> allEvents = webhookEventRepository
                    .findByStatus(WebhookEventStatus.FAILED_RETRYING);
            Integer averageRetryCount = allEvents.isEmpty() ? 0 :
                    (int) allEvents.stream()
                            .mapToInt(WebhookProcessingEvent::getRetryCount)
                            .average()
                            .orElse(0);

            // Find oldest pending event
            LocalDateTime oldestPendingDate = allEvents.isEmpty() ? null :
                    allEvents.stream()
                            .map(WebhookProcessingEvent::getCreatedAt)
                            .min(LocalDateTime::compareTo)
                            .orElse(null);

            // Find most common event type (would need to query all and count)
            String mostCommonEventType = "customer.subscription.created"; // Placeholder

            log.info("✓ Webhook metrics: success={}, retrying={}, deadLetter={}, successRate={}%, avgRetry={}",
                    successCount, failedRetryingCount, deadLetterCount,
                    String.format("%.2f", successRate), averageRetryCount);

            return new WebhookMetricsDTO(
                    successCount,
                    failedRetryingCount,
                    deadLetterCount,
                    successRate,
                    averageRetryCount,
                    oldestPendingDate,
                    mostCommonEventType,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("Error fetching webhook metrics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch webhook metrics", e);
        }
    }
}




