package com.digitalearn.npaxis.subscription.billing.invoice;

import java.time.LocalDateTime;

public record InvoiceResponse(
        Long invoiceId,
        String stripeInvoiceId,
        String status,
        Long amountDueInMinorUnits,
        Long amountPaidInMinorUnits,
        String currency,
        LocalDateTime invoiceDate,
        LocalDateTime invoicePaidAt,
        String invoicePdfUrl,
        String hostedInvoiceUrl
) {
}

