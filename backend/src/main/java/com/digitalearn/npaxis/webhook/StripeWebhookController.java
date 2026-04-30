package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.common.responses.GenericApiResponse;
import com.digitalearn.npaxis.common.responses.ResponseHandler;
import com.digitalearn.npaxis.subscription.config.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.digitalearn.npaxis.utils.APIConstants.BASE_API;
import static com.digitalearn.npaxis.utils.APIConstants.WEBHOOKS_API;
import static com.digitalearn.npaxis.utils.APIConstants.WEBHOOK_EVENTS_API;

/**
 * Webhook endpoint for receiving Stripe events
 * Handles signature verification and event processing
 */
@RestController
@RequestMapping(BASE_API + "/" + WEBHOOKS_API)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Webhook endpoints for Stripe events")
public class StripeWebhookController {

    private final WebhookService webhookService;
    private final StripeProperties stripeProperties;

    /**
     * Receive and process Stripe webhook events
     */
    @PostMapping
    @Operation(summary = "Receive Stripe webhook events")
    public ResponseEntity<GenericApiResponse<Void>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        log.info("Received Stripe webhook");

        try {
            // Verify webhook signature
            Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    stripeProperties.getWebhookSecret()
            );

            log.info("Webhook signature verified. Event: {} - {}", event.getId(), event.getType());

            // Process the event asynchronously
            webhookService.process(event, payload);

            return ResponseHandler.generateResponse(
                    null,
                    "Webhook processed successfully",
                    true,
                    HttpStatus.OK
            );

        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseHandler.generateResponse(
                    null,
                    "Invalid webhook signature",
                    false,
                    HttpStatus.UNAUTHORIZED
            );

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            // Return 200 OK to Stripe even on error to prevent retries
            return ResponseHandler.generateResponse(
                    null,
                    "Webhook received",
                    true,
                    HttpStatus.OK
            );
        }
    }

    /**
     * Get webhook event processing history
     */
    @GetMapping(WEBHOOK_EVENTS_API)
    @Operation(summary = "Get webhook event history")
    public ResponseEntity<GenericApiResponse<Page<WebhookEventResponse>>> getWebhookHistory(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Fetching webhook event history");

        Page<WebhookEventResponse> events = webhookService.getEventHistory(pageable);

        return ResponseHandler.<Page<WebhookEventResponse>>generatePaginatedResponse(
                events,
                events,
                "Webhook history retrieved successfully",
                true,
                HttpStatus.OK
        );
    }
}




