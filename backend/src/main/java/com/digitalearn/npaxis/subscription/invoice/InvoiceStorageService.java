package com.digitalearn.npaxis.subscription.invoice;

import org.springframework.stereotype.Service;

/**
 * Invoice storage service interface for extensibility.
 * <p>
 * Supports multiple storage backends:
 * - Local filesystem (current implementation)
 * - Amazon S3 (future)
 * - Google Cloud Storage (future)
 * - Other cloud providers
 * <p>
 * This design allows for easy switching between storage backends
 * without changing the InvoiceService code.
 */
@Service
public class InvoiceStorageService {

    private static final String INVOICES_DIR = "invoices";

    /**
     * Resolve the storage path for an invoice file.
     *
     * @param invoiceId Stripe invoice ID
     * @return Full file path where the invoice PDF should be stored
     */
    public String resolvePath(String invoiceId) {
        return INVOICES_DIR + "/" + invoiceId + ".pdf";
    }

    /**
     * Get the base storage directory.
     *
     * @return Base directory path
     */
    public String getBaseDir() {
        return INVOICES_DIR;
    }
}

