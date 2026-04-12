package com.digitalearn.npaxis.subscription.core;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(target = "planCode", source = "plan.code")
    @Mapping(target = "billingInterval", source = "price.billingInterval")
    SubscriptionStatusResponse toResponse(PreceptorSubscription entity);
}