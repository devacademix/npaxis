package com.digitalearn.npaxis.subscription.billing.invoice;

import java.time.LocalDateTime;

public record BillingInvoiceResponse(
        String stripeInvoiceId,
        Long amountPaidInMinorUnits,
        Long amountDueInMinorUnits,
        String currency,
        String status,
        String hostedInvoiceUrl,
        String invoicePdfUrl,
        LocalDateTime invoiceCreatedAt,
        LocalDateTime invoicePaidAt
) {
}