package com.digitalearn.npaxis.subscription.mapper;

import com.digitalearn.npaxis.subscription.dto.SubscriptionResponse;
import com.digitalearn.npaxis.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionMapper {

    @Mapping(source = "planPrice.stripePriceId", target = "stripePriceId")
    SubscriptionResponse toResponse(Subscription subscription);
}
