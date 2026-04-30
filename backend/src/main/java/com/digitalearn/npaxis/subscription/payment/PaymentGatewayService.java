package com.digitalearn.npaxis.subscription.payment;

import com.digitalearn.npaxis.subscription.CheckoutSessionRequest;
import com.digitalearn.npaxis.subscription.CheckoutSessionResponse;

public interface PaymentGatewayService {

    /**
     * Initializes a secure hosted checkout session for a subscription.
     *
     * @param request The domain request containing the user ID and billing cycle.
     * @return The URL and Session ID for the hosted payment page.
     */
    CheckoutSessionResponse createCheckoutSession(CheckoutSessionRequest request);
}