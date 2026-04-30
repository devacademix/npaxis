package com.digitalearn.npaxis.subscription.plan;

import com.digitalearn.npaxis.subscription.price.SubscriptionPrice;
import com.digitalearn.npaxis.subscription.price.SubscriptionPriceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {
    SubscriptionPlanResponse toResponse(SubscriptionPlan entity);

    SubscriptionPriceResponse toResponse(SubscriptionPrice entity);
}