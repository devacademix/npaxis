package com.digitalearn.npaxis.webhook;

import com.digitalearn.npaxis.subscription.config.StripeProperties;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stripe/webhook")
public class StripeWebhookController {

    private final StripeProperties properties;
    private final StripeWebhookEventRepository eventRepo;
    private final WebhookService webhookService;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    properties.getWebhookSecret()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        // IDEMPOTENCY CHECK
        if (eventRepo.existsByEventId(event.getId())) {
            return ResponseEntity.ok("Duplicate ignored");
        }

        webhookService.process(event, payload);

        return ResponseEntity.ok("Success");
    }
}