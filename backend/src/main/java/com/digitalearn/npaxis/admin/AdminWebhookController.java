package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.WebhookEventDetailDTO;
import com.digitalearn.npaxis.admin.dto.WebhookMetricsDTO;
import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.webhook.WebhookEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.digitalearn.npaxis.utils.APIConstants.*;

/**
 * Admin controller for webhook management operations
 */
@RestController
@RequestMapping(BASE_API + "/" + ADMINISTRATION_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Webhook Management", description = "Admin-only APIs for webhook administration")
public class AdminWebhookController {

    private final AdminWebhookService adminWebhookService;

    @Operation(summary = "Get webhook event history")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_WEBHOOK_HISTORY_API)
    public ResponseEntity<GenericApiResponse<List<WebhookEventResponse>>> getWebhookHistory(
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("Admin fetching webhook event history");
        Page<WebhookEventResponse> events = adminWebhookService.getWebhookEventHistory(pageable);
        return ResponseHandler.generatePaginatedResponse(
            events,
            events.getContent(),
            "Webhook history fetched successfully",
            true,
            HttpStatus.OK
        );
    }

    @Operation(summary = "Retry failed webhook event")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(ADMIN_WEBHOOK_RETRY_API)
    public ResponseEntity<GenericApiResponse<String>> retryWebhookEvent(
            @PathVariable String eventId) {
        log.info("Admin retrying webhook event - eventId: {}", eventId);
        String result = adminWebhookService.retryWebhookEvent(eventId);
        return ResponseHandler.generateResponse(
            result,
            "Webhook event retry initiated successfully",
            true,
            HttpStatus.OK
        );
    }

    @Operation(summary = "Get webhook event details")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_WEBHOOK_DETAIL_API)
    public ResponseEntity<GenericApiResponse<WebhookEventDetailDTO>> getWebhookEventDetail(
            @PathVariable String eventId) {
        log.info("Admin fetching webhook event detail - eventId: {}", eventId);
        WebhookEventDetailDTO detail = adminWebhookService.getWebhookEventDetail(eventId);
        return ResponseHandler.generateResponse(
            detail,
            "Webhook event detail fetched successfully",
            true,
            HttpStatus.OK
        );
    }

    @Operation(summary = "Get webhook metrics and statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_WEBHOOK_METRICS_API)
    public ResponseEntity<GenericApiResponse<WebhookMetricsDTO>> getWebhookMetrics() {
        log.info("Admin fetching webhook metrics");
        WebhookMetricsDTO metrics = adminWebhookService.getWebhookMetrics();
        return ResponseHandler.generateResponse(
            metrics,
            "Webhook metrics fetched successfully",
            true,
            HttpStatus.OK
        );
    }
}

