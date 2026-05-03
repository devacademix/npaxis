package com.digitalearn.npaxis.pdf.service.impl;

import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.analytics.TrackEvent;
import com.digitalearn.npaxis.pdf.exception.PdfGenerationException;
import com.digitalearn.npaxis.pdf.service.ByteStorageService;
import com.digitalearn.npaxis.pdf.service.HtmlToPdfConverter;
import com.digitalearn.npaxis.pdf.service.PdfRequest;
import com.digitalearn.npaxis.pdf.service.PdfService;
import com.digitalearn.npaxis.pdf.service.PdfUtils;
import com.digitalearn.npaxis.pdf.service.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Production-grade PDF generation service implementation.
 * <p>
 * Orchestrates the complete PDF generation pipeline:
 * 1. Validates request
 * 2. Renders Thymeleaf template with dynamic data
 * 3. Converts rendered HTML to PDF using OpenHTMLtoPDF
 * 4. Optionally stores PDF to configured storage backend
 * 5. Returns bytes or storage path
 * <p>
 * Thread-safe and stateless - safe for concurrent usage.
 * All configurations via dependency injection - no static state.
 * <p>
 * ============================================
 * ANALYTICS TRACKING
 * ============================================
 * Tracks PDF generation operations:
 * - RESOURCE_UPLOADED: PDF document generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final TemplateRenderer templateRenderer;
    private final HtmlToPdfConverter htmlToPdfConverter;
    private final ByteStorageService byteStorageService;

    @Override
    @TrackEvent(
            eventType = EventType.RESOURCE_UPLOADED,
            targetIdExpression = "#request.getOutputFileName()",
            metadataExpression = "{'resourceType': 'pdf_document', 'templateName': #request.getTemplateName(), 'fileSize': #result.length}"
    )
    public byte[] generatePdf(PdfRequest request) {
        try {
            validateRequest(request);
            log.info("Starting PDF generation: template={}, outputFile={}",
                    request.getTemplateName(), request.getOutputFileName());

            // Step 1: Render template to HTML
            String html = templateRenderer.renderTemplate(
                    request.getTemplateName(),
                    request.getData()
            );
            log.debug("✓ Template rendered successfully");

            // Step 2: Convert HTML to PDF
            byte[] pdfBytes = htmlToPdfConverter.convertHtmlToPdf(html);
            log.debug("✓ PDF conversion completed (size: {} bytes)", pdfBytes.length);

            log.info("✓ PDF generation successful: template={}, size={} bytes",
                    request.getTemplateName(), pdfBytes.length);

            return pdfBytes;

        } catch (PdfGenerationException e) {
            // Re-throw custom exceptions
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format(
                    "Unexpected error during PDF generation: %s",
                    e.getMessage()
            );
            log.error(errorMsg, e);

            throw new PdfGenerationException(
                    errorMsg,
                    request.getTemplateName(),
                    "PDF pipeline execution failed",
                    e
            );
        }
    }

    @Override
    public String generateAndStorePdf(PdfRequest request) {
        try {
            // Validate request has storage configuration
            if (!request.isStoreFile()) {
                throw new IllegalArgumentException(
                        "Request has storeFile=false. Use generatePdf() instead for in-memory generation."
                );
            }

            if (!hasText(request.getStorageSubDirectory())) {
                throw new IllegalArgumentException(
                        "storageSubDirectory must be set when storeFile is true"
                );
            }

            log.info("Starting PDF generation with storage: template={}, subDir={}, outputFile={}",
                    request.getTemplateName(),
                    request.getStorageSubDirectory(),
                    request.getOutputFileName()
            );

            // Step 1: Generate PDF bytes
            byte[] pdfBytes = generatePdf(request);
            log.debug("✓ PDF generated: {} bytes", pdfBytes.length);

            // Step 2: Generate safe file name
            String fileName = PdfUtils.generateSafeFileName(request.getOutputFileName());
            log.debug("Using safe file name: {}", fileName);

            // Step 3: Store to storage backend
            String storagePath = byteStorageService.storeBytes(
                    pdfBytes,
                    request.getStorageSubDirectory(),
                    fileName,
                    "application/pdf"
            );

            log.info("✓ PDF generated and stored successfully: path={}",
                    storagePath);

            return storagePath;

        } catch (IllegalArgumentException e) {
            log.warn("Invalid PDF storage request: {}", e.getMessage());
            throw e;
        } catch (PdfGenerationException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format(
                    "Failed to generate and store PDF: %s",
                    e.getMessage()
            );
            log.error(errorMsg, e);

            throw new PdfGenerationException(
                    errorMsg,
                    request.getTemplateName(),
                    "PDF generation and storage failed",
                    e
            );
        }
    }

    /**
     * Validates that the PDF request is valid and complete.
     *
     * @param request the request to validate
     * @throws IllegalArgumentException if request is invalid
     */
    private void validateRequest(PdfRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("PdfRequest cannot be null");
        }

        if (!hasText(request.getTemplateName())) {
            throw new IllegalArgumentException("Template name cannot be empty");
        }

        if (!hasText(request.getOutputFileName())) {
            throw new IllegalArgumentException("Output file name cannot be empty");
        }

        // Validate template and file name patterns
        PdfUtils.validateTemplateName(request.getTemplateName());
        PdfUtils.validateOutputFileName(request.getOutputFileName());
    }

    /**
     * Null-safe string check.
     */
    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }
}

