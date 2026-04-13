package com.digitalearn.npaxis.subscription.core;

import jakarta.validation.constraints.NotNull;

public record UpdateSubscriptionRequest(
        @NotNull(message = "Price ID is required")
        Long priceId
) {
}

