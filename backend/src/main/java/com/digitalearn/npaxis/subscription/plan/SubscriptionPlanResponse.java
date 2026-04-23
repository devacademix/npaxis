package com.digitalearn.npaxis.subscription.plan;

import com.digitalearn.npaxis.subscription.price.SubscriptionPriceResponse;

import java.util.List;

public record SubscriptionPlanResponse(
        Long subscriptionPlanId,
        String code,
        String name,
        String description,
        boolean active,
        List<SubscriptionPriceResponse> prices
) {
}