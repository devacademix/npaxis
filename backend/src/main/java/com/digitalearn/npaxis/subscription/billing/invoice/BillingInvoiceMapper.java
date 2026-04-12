package com.digitalearn.npaxis.subscription.billing.invoice;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BillingInvoiceMapper {
    BillingInvoiceResponse toResponse(BillingInvoice entity);
}