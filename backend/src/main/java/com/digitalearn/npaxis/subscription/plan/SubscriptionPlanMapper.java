package com.digitalearn.npaxis.subscription.plan;

import com.digitalearn.npaxis.subscription.price.SubscriptionPrice;
import com.digitalearn.npaxis.subscription.price.SubscriptionPriceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {
    SubscriptionPlanResponse toResponse(SubscriptionPlan entity);

    @Mapping(source = "id", target = "subscriptionPriceId")
    SubscriptionPriceResponse toResponse(SubscriptionPrice entity);
}