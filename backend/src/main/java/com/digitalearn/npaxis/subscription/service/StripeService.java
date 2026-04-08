package com.digitalearn.npaxis.subscription.service;

import com.digitalearn.npaxis.user.User;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Subscription;

public interface StripeService {
    Customer createCustomer(User user);
    Subscription createSubscription(String customerId, String priceId);
    Subscription cancelSubscription(String subscriptionId);
    Subscription retrieveSubscription(String subscriptionId);
    Event constructWebhookEvent(String payload, String signature);
}
