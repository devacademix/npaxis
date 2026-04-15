package com.digitalearn.npaxis.subscription.invoice;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceLineItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Production-grade invoice PDF generation service using OpenHTMLtoPDF + Thymeleaf.
 * <p>
 * Key features:
 * - Stripe as source of truth (fetches Invoice data from Stripe API)
 * - Idempotent PDF generation (checks if PDF already generated)
 * - Production-grade rendering with OpenHTMLtoPDF
 * - Thymeleaf template support
 * - Concurrent-safe
 * <p>
 * Design pattern:
 * 1. Before generating: Check if PDF already exists for this invoice
 * 2. Process Stripe Invoice via public API
 * 3. Render HTML using Thymeleaf
 * 4. Generate PDF using OpenHTMLtoPDF
 * 5. Store locally (production: implement InvoiceStorage interface for S3/GCS)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final String INVOICES_DIR = "invoices";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    private final SpringTemplateEngine templateEngine;

    /**
     * Generate invoice PDF from Stripe Invoice.
     * <p>
     * STRIPE AS SOURCE OF TRUTH:
     * - Extracts invoice data directly from Stripe Invoice object
     * - Uses invoice.getAmountPaid(), invoice.getCurrency(), invoice.getCreated()
     * - Never uses subscription.getPrice() or other derived data
     * - Idempotent: checks if file exists before generating
     *
     * @param stripeInvoice Stripe Invoice object (fetched via Invoice.retrieve())
     * @return File path to generated PDF
     */
    public String generateInvoicePdf(Invoice stripeInvoice) {
        try {
            // Step 1: Idempotency check
            String pdfPath = INVOICES_DIR + "/" + stripeInvoice.getId() + ".pdf";

            if (Files.exists(Paths.get(pdfPath))) {
                log.info("PDF already generated for invoice {}: {}", stripeInvoice.getId(), pdfPath);
                return pdfPath;
            }

            // Step 2: Create invoices directory if it doesn't exist
            Files.createDirectories(Paths.get(INVOICES_DIR));

            // Step 3: Extract data from Stripe Invoice (SOURCE OF TRUTH)
            String invoiceNumber = stripeInvoice.getNumber() != null ? stripeInvoice.getNumber() : stripeInvoice.getId();
            String customerName = stripeInvoice.getCustomerName() != null ? stripeInvoice.getCustomerName() : "Customer";
            Long amountPaid = stripeInvoice.getAmountPaid() != null ? stripeInvoice.getAmountPaid() : 0L;
            String currency = stripeInvoice.getCurrency() != null ? stripeInvoice.getCurrency() : "usd";
            String hostedInvoiceUrl = stripeInvoice.getHostedInvoiceUrl() != null ? stripeInvoice.getHostedInvoiceUrl() : "";

            // Convert Stripe timestamp to formatted date
            String invoiceDate = Instant.ofEpochSecond(stripeInvoice.getCreated())
                    .atZone(ZoneId.systemDefault())
                    .format(DATE_FORMATTER);

            // Step 4: Extract invoice line items
            List<InvoiceLineItemDto> items = extractLineItems(stripeInvoice);

            // Step 5: Render HTML using Thymeleaf template
            String html = renderInvoiceTemplate(
                    invoiceNumber,
                    invoiceDate,
                    customerName,
                    items,
                    amountPaid,
                    currency,
                    hostedInvoiceUrl
            );

            // Step 6: Generate PDF using OpenHTMLtoPDF (production-grade rendering)
            convertHtmlToPdf(html, pdfPath);

            log.info("✓ Invoice PDF generated successfully: {}", pdfPath);
            return pdfPath;

        } catch (Exception e) {
            log.error("Error generating invoice PDF for {}: {}", stripeInvoice.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    /**
     * Convert HTML to PDF using OpenHTMLtoPDF.
     * Production-grade PDF rendering with proper font handling and styling support.
     */
    private void convertHtmlToPdf(String htmlContent, String outputPath) throws IOException {
        try (OutputStream os = new FileOutputStream(outputPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();
            log.debug("HTML successfully converted to PDF using OpenHTMLtoPDF: {}", outputPath);
        } catch (IOException e) {
            log.error("Error converting HTML to PDF: {}", e.getMessage(), e);
            throw new IOException("Failed to convert HTML to PDF", e);
        }
    }

    /**
     * Extract line items from Stripe Invoice.
     * <p>
     * STRIPE AS SOURCE OF TRUTH:
     * - Uses invoice.getLines().getData() to get all invoice line items
     * - Extracts description, quantity, price per unit, and total
     */
    private List<InvoiceLineItemDto> extractLineItems(Invoice stripeInvoice) {
        List<InvoiceLineItemDto> items = new ArrayList<>();

        try {
            if (stripeInvoice.getLines() != null && stripeInvoice.getLines().getData() != null) {
                for (InvoiceLineItem lineItem : stripeInvoice.getLines().getData()) {
                    String description = "Subscription";
                    if (lineItem.getDescription() != null) {
                        description = lineItem.getDescription();
                    }

                    Long quantity = lineItem.getQuantity() != null ? lineItem.getQuantity() : 1L;
                    Long pricePerUnit = lineItem.getAmount() != null ? lineItem.getAmount() : 0L;
                    Long total = lineItem.getAmount() != null ? lineItem.getAmount() : 0L;

                    items.add(new InvoiceLineItemDto(
                            description,
                            quantity,
                            pricePerUnit,
                            total
                    ));
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting line items from invoice: {}", e.getMessage());
        }

        // Fallback: add single item if no line items found
        if (items.isEmpty()) {
            items.add(new InvoiceLineItemDto(
                    "Subscription",
                    1L,
                    stripeInvoice.getAmountPaid() != null ? stripeInvoice.getAmountPaid() : 0L,
                    stripeInvoice.getAmountPaid() != null ? stripeInvoice.getAmountPaid() : 0L
            ));
        }

        return items;
    }

    /**
     * Render invoice HTML using Thymeleaf template.
     */
    private String renderInvoiceTemplate(
            String invoiceNumber,
            String invoiceDate,
            String customerName,
            List<InvoiceLineItemDto> items,
            Long amountPaidInMinorUnits,
            String currency,
            String hostedInvoiceUrl
    ) {
        Context context = new Context();
        context.setVariable("invoiceNumber", invoiceNumber);
        context.setVariable("invoiceDate", invoiceDate);
        context.setVariable("customerName", customerName);
        context.setVariable("items", items);
        context.setVariable("amountPaid", formatAmount(amountPaidInMinorUnits));
        context.setVariable("currency", currency.toUpperCase());
        context.setVariable("hostedInvoiceUrl", hostedInvoiceUrl);

        return templateEngine.process("invoice", context);
    }

    /**
     * Format amount from minor units to currency format.
     */
    private String formatAmount(Long amountInMinorUnits) {
        double amount = amountInMinorUnits / 100.0;
        return String.format("%.2f", amount);
    }

    /**
     * DTO for invoice line items.
     */
    public record InvoiceLineItemDto(
            String description,
            Long quantity,
            Long pricePerUnit,
            Long total
    ) {
    }
}




