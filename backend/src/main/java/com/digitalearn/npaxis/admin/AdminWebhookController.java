package com.digitalearn.npaxis.admin;

import com.digitalearn.npaxis.admin.dto.WebhookEventDetailDTO;
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
@RequestMapping(ADMINISTRATION_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Webhook Management", description = "Admin-only APIs for webhook administration")
public class AdminWebhookController {

    @Operation(summary = "Get webhook event history")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_WEBHOOK_HISTORY_API)
    public ResponseEntity<GenericApiResponse<List<WebhookEventResponse>>> getWebhookHistory(
            @PageableDefault(size = 50) Pageable pageable) {
        log.info("Admin fetching webhook event history");
        // Placeholder - would integrate with WebhookService
        return ResponseHandler.generateResponse(
                List.of(),
                "Webhook history fetched successfully",
                true,
                HttpStatus.OK);
    }

    @Operation(summary = "Retry failed webhook event")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(ADMIN_WEBHOOK_RETRY_API)
    public ResponseEntity<GenericApiResponse<String>> retryWebhookEvent(
            @PathVariable String eventId) {
        log.info("Admin retrying webhook event - eventId: {}", eventId);
        // Placeholder - would integrate with WebhookService
        return ResponseHandler.generateResponse(
                "Webhook event retry initiated",
                "Webhook event retry initiated successfully",
                true,
                HttpStatus.OK);
    }

    @Operation(summary = "Get webhook event details")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_WEBHOOK_DETAIL_API)
    public ResponseEntity<GenericApiResponse<WebhookEventDetailDTO>> getWebhookEventDetail(
            @PathVariable String eventId) {
        log.info("Admin fetching webhook event detail - eventId: {}", eventId);
        // Placeholder - would integrate with WebhookService
        return ResponseHandler.generateResponse(
                null,
                "Webhook event detail fetched successfully",
                true,
                HttpStatus.OK);
    }
}

