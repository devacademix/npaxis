package com.digitalearn.npaxis.subscription.webhook;

import com.digitalearn.npaxis.subscription.entity.WebhookEvent;
import com.digitalearn.npaxis.subscription.repository.WebhookEventRepository;
import com.digitalearn.npaxis.subscription.service.StripeService;
import com.digitalearn.npaxis.subscription.service.SubscriptionService;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final StripeService stripeService;
    private final SubscriptionService subscriptionService;
    private final WebhookEventRepository webhookEventRepository;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        log.info("Received Stripe webhook");

        try {
            Event event = stripeService.constructWebhookEvent(payload, signature);

            if (webhookEventRepository.findByStripeEventId(event.getId()).isPresent()) {
                log.warn("Duplicate webhook event received: {}", event.getId());
                return ResponseEntity.ok("Duplicate event");
            }

            WebhookEvent webhookEvent = WebhookEvent.builder()
                    .stripeEventId(event.getId())
                    .type(event.getType())
                    .payload(payload)
                    .processed(false)
                    .build();
            webhookEventRepository.save(webhookEvent);

            subscriptionService.processWebhook(event);

            webhookEvent.setProcessed(true);
            webhookEventRepository.save(webhookEvent);

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }
}
