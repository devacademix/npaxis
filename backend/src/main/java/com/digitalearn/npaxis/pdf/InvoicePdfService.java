package com.digitalearn.npaxis.pdf;

/**
 * High-level contract for invoice PDF generation and storage.
 * <p>
 * Responsibilities:
 * 1. Render invoice templates to HTML
 * 2. Generate PDF from HTML
 * 3. Store PDF via StorageService (backend-agnostic)
 * 4. Return file path/URL for later retrieval
 * <p>
 * PRODUCTION DESIGN:
 * - Abstracts invoice business logic from PDF rendering
 * - Delegates to PdfGenerationService for rendering
 * - Delegates to StorageService for persistence
 * - Supports idempotent operations
 * - No direct file I/O (all via StorageService)
 */
public interface InvoicePdfService {

    /**
     * Generate invoice PDF from Stripe Invoice data.
     * <p>
     * This method is idempotent - calling it multiple times with the same invoice ID
     * will return the same file path without regenerating the PDF.
     *
     * @param request The invoice PDF request with all necessary data
     * @return File path/URL where the PDF is stored (via StorageService)
     * @throws InvoicePdfException if generation or storage fails
     */
    String generateInvoicePdf(InvoicePdfRequest request) throws InvoicePdfException;

    /**
     * Generate invoice PDF from subscription event data.
     * <p>
     * Convenience method for subscription lifecycle events (created, upgraded, canceled).
     * Builds an InvoicePdfRequest internally and delegates to generateInvoicePdf().
     *
     * @param subscription The preceptor subscription
     * @param eventType The subscription event type (SUBSCRIPTION_CREATED, SUBSCRIPTION_UPGRADED, etc.)
     * @return File path/URL where the PDF is stored
     * @throws InvoicePdfException if generation or storage fails
     */
    String generateSubscriptionInvoicePdf(
            com.digitalearn.npaxis.subscription.core.PreceptorSubscription subscription,
            String eventType
    ) throws InvoicePdfException;

    /**
     * Delete an invoice PDF from storage.
     *
     * @param filePath The file path/URL returned by generateInvoicePdf()
     * @throws InvoicePdfException if deletion fails
     */
    void deleteInvoicePdf(String filePath) throws InvoicePdfException;

    /**
     * Exception thrown when invoice PDF operations fail.
     */
    class InvoicePdfException extends Exception {
        public InvoicePdfException(String message) {
            super(message);
        }

        public InvoicePdfException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

