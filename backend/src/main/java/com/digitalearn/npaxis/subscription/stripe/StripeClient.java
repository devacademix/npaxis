package com.digitalearn.npaxis.subscription.stripe;

import com.digitalearn.npaxis.subscription.config.StripeProperties;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.billingportal.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeClient {

    private final StripeProperties stripeProperties;

    /**
     * Ensures Stripe SDK is initialized before every call
     */
    private void init() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        Stripe.setMaxNetworkRetries(stripeProperties.getMaxNetworkRetries());
        Stripe.setConnectTimeout(stripeProperties.getConnectTimeoutMs());
        Stripe.setReadTimeout(stripeProperties.getReadTimeoutMs());
    }

    public Customer createCustomer(String email) throws StripeException {
        init();

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .build();

        return Customer.create(params);
    }

    public Session createCheckoutSession(
            com.stripe.param.checkout.SessionCreateParams params
    ) throws StripeException {
        init();
        return Session.create(params);
    }

    public Subscription retrieveSubscription(String subscriptionId) throws StripeException {
        init();
        return Subscription.retrieve(subscriptionId);
    }

    public Subscription cancelSubscription(String subscriptionId) throws StripeException {
        init();
        Subscription sub = Subscription.retrieve(subscriptionId);
        return sub.cancel();
    }

    public com.stripe.model.billingportal.Session createCustomerPortal(
            SessionCreateParams params
    ) throws StripeException {
        init();
        return com.stripe.model.billingportal.Session.create(params);
    }
}