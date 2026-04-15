package com.digitalearn.npaxis.pdf;

/**
 * DTO for subscription invoice email processing.
 * <p>
 * This record is designed to be detached from the Hibernate session,
 * preventing lazy-loading issues and enabling safe async email processing.
 * <p>
 * All data is extracted and serialized INSIDE a transaction context,
 * so the async email sender doesn't need to access the database.
 */
public record SubscriptionInvoiceEmailDto(
        Long preceptorId,
        String preceptorName,
        String email,
        String planName,
        String invoicePdfPath,
        String hostedInvoiceUrl,
        Long amountPaidInMinorUnits,
        String currency,
        String invoiceNumber
) {
}

