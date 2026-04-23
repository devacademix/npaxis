package com.digitalearn.npaxis.subscription.payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(source = "id", target = "paymentId")
    PaymentResponse toResponse(Payment payment);

    // You can add list mappings here too:
    // List<PaymentResponse> toResponseList(List<Payment> payments);
}