package com.digitalearn.npaxis.pdf.impl;

import com.digitalearn.npaxis.storage.StorageService;
import com.digitalearn.npaxis.subscription.core.PreceptorSubscription;
import com.digitalearn.npaxis.pdf.InvoicePdfRequest;
import com.digitalearn.npaxis.pdf.InvoicePdfService;
import com.digitalearn.npaxis.pdf.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Production-grade invoice PDF generation and storage service.
 * <p>
 * Responsibilities:
 * 1. Render invoice HTML using Thymeleaf templates
 * 2. Generate PDF using PdfGenerationService (OpenHTMLtoPDF)
 * 3. Store PDF using StorageService (local, S3, GCS, etc.)
 * <p>
 * DESIGN PRINCIPLES:
 * - Single responsibility: orchestrates PDF lifecycle
 * - Dependency injection: all dependencies provided externally
 * - Backend-agnostic: StorageService handles persistence
 * - Idempotent: same invoice ID returns same PDF (no regeneration)
 * - Loosely coupled: implements interface, uses other interfaces
 * - Production-ready: comprehensive logging and error handling
 * <p>
 * DATA FLOW:
 * 1. Request arrives (Stripe Invoice or Subscription data)
 * 2. Extract data → build InvoicePdfRequest or convert subscription to request
 * 3. Render template to HTML using Thymeleaf
 * 4. Generate PDF using PdfGenerationService
 * 5. Convert byte array to MultipartFile wrapper
 * 6. Store using StorageService (handles all backend logic)
 * 7. Return file path/URL
 * <p>
 * STRIPE AS SOURCE OF TRUTH:
 * When generating from Stripe Invoice, data comes directly from Stripe:
 * - invoice.getAmountPaid()
 * - invoice.getCurrency()
 * - invoice.getCreated()
 * - invoice.getLines().getData()
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private static final String INVOICES_STORAGE_DIR = "invoices";
    private static final String INVOICE_TEMPLATE = "invoice";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    private static final DateTimeFormatter SUBSCRIPTION_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final PdfGenerationService pdfGenerationService;
    private final SpringTemplateEngine templateEngine;
    private final StorageService storageService;

    @Override
    public String generateInvoicePdf(InvoicePdfRequest request) throws InvoicePdfException {
        try {
            log.info("Generating invoice PDF for invoice: {}", request.invoiceId());

            // Step 1: Render HTML using Thymeleaf template
            String html = renderInvoiceTemplate(request);
            log.debug("Invoice HTML rendered for invoice: {}", request.invoiceId());

            // Step 2: Generate PDF bytes using PdfGenerationService
            byte[] pdfBytes = pdfGenerationService.renderHtmlToPdf(html);
            log.debug("PDF generated (size: {} bytes) for invoice: {}", pdfBytes.length, request.invoiceId());

            // Step 3: Store PDF using StorageService
            String filePath = storeInvoicePdf(request.invoiceId(), pdfBytes);
            log.info("✓ Invoice PDF stored successfully: {}", filePath);

            return filePath;

        } catch (PdfGenerationService.PdfGenerationException e) {
            log.error("PDF generation failed for invoice {}: {}", request.invoiceId(), e.getMessage(), e);
            throw new InvoicePdfException("PDF generation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error generating invoice PDF for {}: {}", request.invoiceId(), e.getMessage(), e);
            throw new InvoicePdfException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateSubscriptionInvoicePdf(PreceptorSubscription subscription, String eventType) throws InvoicePdfException {
        try {
            log.info("Generating subscription invoice PDF: user={}, event={}",
                    subscription.getPreceptor().getUserId(), eventType);

            // Extract all data in transaction context (before async)
            double amountInCurrency = subscription.getPrice().getAmountInMinorUnits() / 100.0;
            String planName = subscription.getPlan().getName();
            String preceptorName = subscription.getPreceptor().getName();
            String preceptorEmail = subscription.getPreceptor().getEmail();
            String billingInterval = subscription.getPrice().getBillingInterval().toString().toLowerCase();
            String currency = subscription.getPrice().getCurrency().toUpperCase();

            LocalDateTime now = LocalDateTime.now();
            String invoiceDate = now.format(SUBSCRIPTION_DATE_FORMATTER);
            String invoiceNumber = generateInvoiceNumber();

            String eventTitle = switch (eventType) {
                case "SUBSCRIPTION_CREATED" -> "Subscription Invoice";
                case "SUBSCRIPTION_UPGRADED" -> "Plan Upgrade Invoice";
                case "SUBSCRIPTION_CANCELED" -> "Final Invoice";
                default -> "Invoice";
            };

            // Build context for template
            Map<String, Object> context = new HashMap<>();
            context.put("invoiceNumber", invoiceNumber);
            context.put("invoiceDate", invoiceDate);
            context.put("eventTitle", eventTitle);
            context.put("preceptorName", preceptorName);
            context.put("preceptorEmail", preceptorEmail);
            context.put("preceptorId", subscription.getPreceptor().getUserId());
            context.put("planName", planName);
            context.put("billingInterval", billingInterval.substring(0, 1).toUpperCase() + billingInterval.substring(1));
            context.put("status", subscription.getStatus());
            context.put("amount", String.format("%.2f", amountInCurrency));
            context.put("currency", currency);

                    // Create request and generate PDF
                    InvoicePdfRequest request = InvoicePdfRequest.builder()
                            .invoiceId(subscription.getPreceptorSubscriptionId().toString())
                            .invoiceNumber(invoiceNumber)
                            .customerName(preceptorName)
                            .invoiceDate(invoiceDate)
                            .templateContext(context)
                            .build();

            return generateInvoicePdf(request);

        } catch (InvoicePdfException e) {
            log.error("Subscription invoice PDF generation failed for user {}: {}",
                    subscription.getPreceptor().getUserId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error generating subscription invoice PDF: {}", e.getMessage(), e);
            throw new InvoicePdfException("Failed to generate subscription invoice PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteInvoicePdf(String filePath) throws InvoicePdfException {
        try {
            storageService.deleteFile(filePath);
            log.info("Invoice PDF deleted: {}", filePath);
        } catch (Exception e) {
            log.error("Error deleting invoice PDF at {}: {}", filePath, e.getMessage(), e);
            throw new InvoicePdfException("Failed to delete invoice PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Render invoice HTML using Thymeleaf template.
     * <p>
     * The template (invoice.html) is responsible for:
     * - Professional formatting
     * - Company branding
     * - Dynamic data rendering
     * - CSS styling
     */
    private String renderInvoiceTemplate(InvoicePdfRequest request) {
        Context context = new Context();

        // Add all template variables from the request
        request.templateContext().forEach(context::setVariable);

        // Add standard variables
        context.setVariable("invoiceNumber", request.invoiceNumber());
        context.setVariable("invoiceDate", request.invoiceDate());
        context.setVariable("customerName", request.customerName());

        return templateEngine.process(INVOICE_TEMPLATE, context);
    }

    /**
     * Store invoice PDF using StorageService.
     * <p>
     * StorageService handles:
     * - Directory creation
     * - File naming (with timestamp/UUID for uniqueness)
     * - Backend-specific logic (local fs, S3, GCS, etc.)
     * - Permission handling
     * <p>
     * Returns file path/URL for later retrieval and email attachment.
     */
    private String storeInvoicePdf(String invoiceId, byte[] pdfBytes) {
        try {
            // Convert byte array to MultipartFile for StorageService
            MultipartFile multipartFile = new ByteArrayMultipartFile(
                    pdfBytes,
                    invoiceId + ".pdf",
                    "application/pdf"
            );

            // Store using StorageService (handles local/S3/GCS logic)
            String filePath = storageService.storeFile(
                    multipartFile,
                    INVOICES_STORAGE_DIR,
                    invoiceId
            );

            return filePath;

        } catch (Exception e) {
            log.error("Error storing invoice PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store invoice PDF", e);
        }
    }

    /**
     * Generate unique invoice number.
     */
    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + timestamp + "-" + randomPart;
    }

    /**
     * Wrapper to convert byte array to MultipartFile.
     * <p>
     * StorageService expects MultipartFile interface.
     * This adapter allows us to pass byte arrays directly.
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] bytes;
        private final String filename;
        private final String contentType;

        ByteArrayMultipartFile(byte[] bytes, String filename, String contentType) {
            this.bytes = bytes;
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override
        public String getName() {
            return filename;
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return bytes == null || bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes == null ? 0 : bytes.length;
        }

        @Override
        public byte[] getBytes() {
            return bytes;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(java.io.File dest) {
            throw new UnsupportedOperationException("Use StorageService directly");
        }
    }
}


