package com.digitalearn.npaxis.subscription.service;

import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;

public interface StripeService {
    Customer createCustomer(String email, String name, String userExternalId);

    Subscription createSubscription(String customerId, String priceId, String idempotencyKey);

    Subscription cancelSubscription(String stripeSubscriptionId, boolean invoiceNow);

    Subscription retrieveSubscription(String stripeSubscriptionId);

    Event constructEvent(String payload, String signatureHeader);

    Subscription updateSubscription(String stripeSubscriptionId, SubscriptionUpdateParams params);
}
