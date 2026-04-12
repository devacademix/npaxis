package com.digitalearn.npaxis.subscription.price;

public record SubscriptionPriceResponse(
        Long subscriptionPriceId,
        String billingInterval,
        String currency,
        Long amountInMinorUnits,
        boolean active
) {
}