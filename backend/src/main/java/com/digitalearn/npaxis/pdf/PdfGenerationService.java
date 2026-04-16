package com.digitalearn.npaxis.pdf;

/**
 * Contract for PDF generation from HTML content.
 * <p>
 * This interface abstracts PDF rendering technology allowing:
 * - Multiple implementations (OpenHTMLtoPDF, iText, etc.)
 * - Easy testing via mocks
 * - Technology swap without affecting consumers
 * <p>
 * PRODUCTION DESIGN:
 * - Single responsibility: HTML → PDF rendering only
 * - No direct file I/O (delegates to StorageService)
 * - Stateless and thread-safe
 * - No side effects
 */
public interface PdfGenerationService {

    /**
     * Render HTML content to PDF bytes.
     *
     * @param htmlContent The HTML content to render
     * @return PDF document as byte array
     * @throws PdfGenerationException if rendering fails
     */
    byte[] renderHtmlToPdf(String htmlContent) throws PdfGenerationException;

    /**
     * Exception thrown when PDF generation fails.
     */
    class PdfGenerationException extends Exception {
        public PdfGenerationException(String message) {
            super(message);
        }

        public PdfGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

