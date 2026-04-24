package com.digitalearn.npaxis.pdf.impl;

import com.digitalearn.npaxis.pdf.PdfGenerationService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Production-grade PDF renderer using OpenHTMLtoPDF.
 * <p>
 * Features:
 * - Renders HTML with proper CSS and font support
 * - Professional PDF output quality
 * - Memory-efficient byte array output
 * - Comprehensive error handling
 * <p>
 * Why OpenHTMLtoPDF:
 * - Supports modern CSS3
 * - Better font handling than older iText
 * - Renders HTML5 documents properly
 * - Production-grade stability
 * - Active maintenance and support
 * <p>
 * DESIGN:
 * - No side effects (no file I/O)
 * - Stateless and thread-safe
 * - Returns byte array for flexible storage (local, S3, GCS, etc.)
 */
@Slf4j
@Service
public class OpenHtmlPdfRenderer implements PdfGenerationService {

    @Override
    public byte[] renderHtmlToPdf(String htmlContent) throws PdfGenerationException {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            throw new PdfGenerationException("HTML content cannot be null or empty");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            convertHtmlToPdf(htmlContent, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error converting HTML to PDF: {}", e.getMessage(), e);
            throw new PdfGenerationException("Failed to convert HTML to PDF", e);
        }
    }

    /**
     * Convert HTML content to PDF using OpenHTMLtoPDF.
     * <p>
     * Production-grade rendering with:
     * - Proper font handling
     * - CSS support
     * - Page layout
     * - Styling
     *
     * @param htmlContent  The HTML content to render
     * @param outputStream The output stream to write PDF bytes to
     * @throws IOException if rendering fails
     */
    private void convertHtmlToPdf(String htmlContent, OutputStream outputStream) throws IOException {
        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();
            log.debug("HTML successfully converted to PDF using OpenHTMLtoPDF");
        } catch (Exception e) {
            log.error("Error in OpenHTMLtoPDF rendering: {}", e.getMessage(), e);
            throw new IOException("Failed to render HTML to PDF", e);
        }
    }
}

