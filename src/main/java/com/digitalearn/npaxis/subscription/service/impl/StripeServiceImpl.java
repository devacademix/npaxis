package com.digitalearn.npaxis.subscription.service.impl;

import com.digitalearn.npaxis.subscription.StripeConfigProperties;
import com.digitalearn.npaxis.subscription.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeServiceImpl implements StripeService {

    private final StripeConfigProperties config;

    @Override
    public Customer createCustomer(String email, String name, String userExternalId) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .putMetadata(\"user_id\", userExternalId)
                    .build();
            return Customer.create(params);
        } catch (StripeException e) {
            log.error(\"Stripe createCustomer failed\", e);
            throw new RuntimeException(\"Unable to create customer\", e);
        }
    }

    @Override
    public Subscription createSubscription(String customerId, String priceId, String idempotencyKey) {
        try {
            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                    .setCustomer(customerId)
                    .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
                    .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                    .addExpand(\"latest_invoice.payment_intent\")
                    .build();

            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey(idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())
                    .build();

            return Subscription.create(params, requestOptions);
        } catch (StripeException e) {
            log.error(\"Stripe createSubscription failed\", e);
            throw new RuntimeException(\"Unable to create subscription\", e);
        }
    }

    @Override
    public Subscription cancelSubscription(String stripeSubscriptionId, boolean invoiceNow) {
        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            if (invoiceNow) {
                subscription.cancel();
            } else {
                subscription = subscription.update(params);
            }
            return subscription;
        } catch (StripeException e) {
            log.error(\"Stripe cancelSubscription failed\", e);
            throw new RuntimeException(\"Unable to cancel subscription\", e);
        }
    }

    @Override
    public Subscription retrieveSubscription(String stripeSubscriptionId) {
        try {
            return Subscription.retrieve(stripeSubscriptionId);
        } catch (StripeException e) {
            log.error(\"Stripe retrieveSubscription failed\", e);
            throw new RuntimeException(\"Unable to retrieve subscription\", e);
        }
    }

    @Override
    public Event constructEvent(String payload, String signatureHeader) {
        try {
            return Webhook.constructEvent(payload, signatureHeader, config.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn(\"Stripe webhook signature verification failed\", e);
            throw new RuntimeException(\"Invalid signature\", e);
        }
    }

    @Override
    public Subscription updateSubscription(String stripeSubscriptionId, SubscriptionUpdateParams params) {
        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            return subscription.update(params);
        } catch (StripeException e) {
            log.error(\"Stripe updateSubscription failed\", e);
            throw new RuntimeException(\"Unable to update subscription\", e);
        }
    }
}
