package com.digitalearn.npaxis.subscription.core;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "planCode", source = "plan.code")
    @Mapping(target = "billingInterval", source = "price.billingInterval")
    SubscriptionStatusResponse toResponse(PreceptorSubscription entity);

    @Mapping(target = "subscriptionId", source = "preceptorSubscriptionId")
    @Mapping(target = "planCode", source = "plan.code")
    @Mapping(target = "planName", source = "plan.name")
    @Mapping(target = "billingInterval", source = "price.billingInterval")
    @Mapping(target = "amountInMinorUnits", source = "price.amountInMinorUnits")
    @Mapping(target = "currency", source = "price.currency")
    @Mapping(target = "status", expression = "java(entity.getStatus().toString())")
    SubscriptionDetailResponse toDetailResponse(PreceptorSubscription entity);

    @Mapping(target = "subscriptionId", source = "preceptorSubscriptionId")
    @Mapping(target = "planCode", source = "plan.code")
    @Mapping(target = "planName", source = "plan.name")
    @Mapping(target = "status", expression = "java(entity.getStatus().toString())")
    @Mapping(target = "startDate", source = "currentPeriodStart")
    @Mapping(target = "endDate", source = "currentPeriodEnd")
    SubscriptionHistoryResponse toHistoryResponse(PreceptorSubscription entity);
}