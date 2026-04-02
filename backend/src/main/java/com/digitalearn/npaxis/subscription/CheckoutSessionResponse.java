package com.digitalearn.npaxis.subscription;

public record CheckoutSessionResponse(
        String sessionId,
        String checkoutUrl
) {
}