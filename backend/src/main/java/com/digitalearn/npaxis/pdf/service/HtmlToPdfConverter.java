package com.digitalearn.npaxis.pdf.service;

import com.digitalearn.npaxis.pdf.exception.PdfGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Converts HTML to PDF using Flying Saucer with IText/PDFBox backend.
 * Handles safe stream management, encoding, and error recovery.
 * Thread-safe and stateless - safe for concurrent usage.
 * <p>
 * Flying Saucer is a mature, industry-standard HTML/XHTML to PDF renderer:
 * - Supports CSS 2.1 styling
 * - Excellent Thymeleaf template support
 * - Thread-safe rendering
 * - Production-proven in many enterprises
 */
@Slf4j
@Component
public class HtmlToPdfConverter {

    /**
     * Converts HTML string to PDF bytes using Flying Saucer with IText backend.
     * Uses UTF-8 encoding and PDFBox for rendering.
     * <p>
     * Performance considerations:
     * - Creates a new ByteArrayOutputStream each call (GC-friendly)
     * - Fresh renderer instance per PDF (thread-safe)
     * - HTML is small enough to fit in memory for typical documents
     *
     * @param htmlContent the HTML content to convert
     * @return PDF as bytes
     * @throws PdfGenerationException if conversion fails at any stage
     */
    public byte[] convertHtmlToPdf(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML content cannot be null or empty");
        }

        try {
            log.debug("Starting HTML to PDF conversion (HTML size: {} chars)", htmlContent.length());

            // Create Flying Saucer renderer
            ITextRenderer renderer = new ITextRenderer();

            // Load HTML content as document
            // Flying Saucer handles:
            // - UTF-8 encoding automatically
            // - XHTML/HTML rendering
            // - CSS styling (including Thymeleaf-rendered rules)
            renderer.setDocumentFromString(htmlContent);

            // Layout the document (required before rendering)
            renderer.layout();

            // Render PDF to memory via ITextRenderer's built-in capabilities
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            try {
                renderer.createPDF(outputStream);
                byte[] pdfBytes = outputStream.toByteArray();

                log.debug("PDF conversion successful (PDF size: {} bytes)", pdfBytes.length);
                PdfUtils.validatePdfSize(pdfBytes);
                return pdfBytes;

            } finally {
                PdfUtils.closeStream(outputStream);
            }

        } catch (PdfGenerationException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            String errorMsg = "Failed to convert HTML to PDF: " + e.getMessage();
            log.error(errorMsg, e);

            throw new PdfGenerationException(
                    errorMsg,
                    "html-converter",
                    "Flying Saucer renderer error",
                    e
            );
        }
    }

    /**
     * Converts HTML with optional sizing constraints.
     * Useful for multi-page PDFs or specific layout requirements.
     * <p>
     * Note: Flying Saucer handles page sizing via CSS @page rules in the HTML.
     *
     * @param htmlContent  the HTML content
     * @param pageWidthMm  page width in millimeters (informational)
     * @param pageHeightMm page height in millimeters (informational)
     * @return PDF as bytes
     * @throws PdfGenerationException if conversion fails
     */
    public byte[] convertHtmlToPdfWithPageSize(String htmlContent, float pageWidthMm, float pageHeightMm) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML content cannot be null or empty");
        }

        try {
            log.debug("Starting HTML to PDF conversion with suggested page size: {}mm x {}mm",
                    pageWidthMm, pageHeightMm);

            // Flying Saucer determines page size from CSS @page rules
            // To set specific sizes, include in HTML:
            // <style>
            //   @page { size: 210mm 297mm; margin: 2cm; }
            // </style>

            return convertHtmlToPdf(htmlContent);

        } catch (PdfGenerationException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = "Failed to convert HTML to PDF with page size: " + e.getMessage();
            log.error(errorMsg, e);

            throw new PdfGenerationException(
                    errorMsg,
                    "html-converter",
                    String.format("Requested size: %.1f x %.1f mm", pageWidthMm, pageHeightMm),
                    e
            );
        }
    }
}






