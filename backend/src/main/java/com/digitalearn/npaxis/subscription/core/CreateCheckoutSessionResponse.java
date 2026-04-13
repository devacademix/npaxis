package com.digitalearn.npaxis.subscription.core;

public record CreateCheckoutSessionResponse(
        String sessionId,
        String checkoutUrl,
        String customerId
) {
}