package com.digitalearn.npaxis.subscription.service;

import com.digitalearn.npaxis.user.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public Customer createCustomer(User user) {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getDisplayName())
                .putMetadata("userId", String.valueOf(user.getUserId()))
                .build();

        try {
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey(UUID.randomUUID().toString())
                    .build();
            return Customer.create(params, options);
        } catch (StripeException e) {
            log.error("Error creating Stripe customer for user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Stripe customer creation failed", e);
        }
    }

    @Override
    public Subscription createSubscription(String customerId, String priceId) {
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(priceId)
                        .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addExpand("latest_invoice.payment_intent")
                .build();

        try {
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey(UUID.randomUUID().toString())
                    .build();
            return Subscription.create(params, options);
        } catch (StripeException e) {
            log.error("Error creating Stripe subscription for customer {}: {}", customerId, e.getMessage());
            throw new RuntimeException("Stripe subscription creation failed", e);
        }
    }

    @Override
    public Subscription cancelSubscription(String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            return subscription.update(params);
        } catch (StripeException e) {
            log.error("Error canceling Stripe subscription {}: {}", subscriptionId, e.getMessage());
            throw new RuntimeException("Stripe subscription cancellation failed", e);
        }
    }

    @Override
    public Subscription retrieveSubscription(String subscriptionId) {
        try {
            return Subscription.retrieve(subscriptionId);
        } catch (StripeException e) {
            log.error("Error retrieving Stripe subscription {}: {}", subscriptionId, e.getMessage());
            throw new RuntimeException("Stripe subscription retrieval failed", e);
        }
    }

    @Override
    public Event constructWebhookEvent(String payload, String signature) {
        try {
            return Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (Exception e) {
            log.error("Error constructing Stripe webhook event: {}", e.getMessage());
            throw new RuntimeException("Stripe webhook verification failed", e);
        }
    }
}
