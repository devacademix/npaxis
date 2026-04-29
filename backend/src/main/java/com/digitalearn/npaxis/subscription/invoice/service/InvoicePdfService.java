package com.digitalearn.npaxis.subscription.invoice.service;

import com.digitalearn.npaxis.pdf.exception.PdfGenerationException;
import com.digitalearn.npaxis.pdf.service.PdfRequest;
import com.digitalearn.npaxis.pdf.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating invoice PDFs.
 * Demonstrates proper usage of the generic PdfService.
 * <p>
 * Responsibilities:
 * - Prepare invoice data from domain objects
 * - Build PdfRequest with invoice-specific template and data
 * - Delegate to PdfService for actual generation
 * - Handle invoice-specific error cases
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private static final String INVOICE_TEMPLATE = "invoice";
    private static final String INVOICES_STORAGE_DIR = "invoices";
    private final PdfService pdfService;

    /**
     * Generates an invoice PDF and returns the raw bytes (in-memory).
     * Useful for email attachments or immediate download.
     *
     * @param invoiceNumber           the invoice number (e.g., "INV-2024-001")
     * @param customerName            the customer/preceptor name
     * @param invoiceDate             the date of the invoice
     * @param items                   list of invoice line items
     * @param totalAmountInMinorUnits total amount in minor units (e.g., cents for USD)
     * @param currency                the currency code (e.g., "USD", "EUR")
     * @param hostedInvoiceUrl        optional Stripe hosted invoice URL
     * @return PDF bytes
     * @throws PdfGenerationException if PDF generation fails
     */
    public byte[] generateInvoicePdfBytes(
            String invoiceNumber,
            String customerName,
            LocalDateTime invoiceDate,
            List<InvoiceItemDto> items,
            Long totalAmountInMinorUnits,
            String currency,
            String hostedInvoiceUrl
    ) {
        try {
            log.info("Generating invoice PDF bytes: invoice={}", invoiceNumber);

            Map<String, Object> data = prepareInvoiceData(
                    invoiceNumber,
                    customerName,
                    invoiceDate,
                    items,
                    totalAmountInMinorUnits,
                    currency,
                    hostedInvoiceUrl
            );

            Map<String, String> metadata = new HashMap<>();
            metadata.put("invoiceId", invoiceNumber);
            metadata.put("currency", currency);

            PdfRequest request = PdfRequest.builder()
                    .templateName(INVOICE_TEMPLATE)
                    .data(data)
                    .outputFileName("invoice-" + invoiceNumber)
                    .storeFile(false) // Return bytes only
                    .metadata(metadata)
                    .build();

            byte[] pdfBytes = pdfService.generatePdf(request);
            log.info("✓ Invoice PDF generated: {} bytes", pdfBytes.length);

            return pdfBytes;

        } catch (PdfGenerationException e) {
            log.error("Failed to generate invoice PDF: {}", invoiceNumber, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating invoice PDF: {}", invoiceNumber, e);
            throw new PdfGenerationException(
                    "Invoice PDF generation failed: " + e.getMessage(),
                    INVOICE_TEMPLATE,
                    "invoice=" + invoiceNumber,
                    e
            );
        }
    }

    /**
     * Generates an invoice PDF and stores it to configured storage.
     * Useful for persistent record-keeping and later retrieval.
     *
     * @param invoiceNumber           the invoice number
     * @param customerName            the customer name
     * @param invoiceDate             the invoice date
     * @param items                   list of line items
     * @param totalAmountInMinorUnits total amount
     * @param currency                the currency code
     * @param hostedInvoiceUrl        optional Stripe URL
     * @return storage path where PDF is stored
     * @throws PdfGenerationException if generation or storage fails
     */
    public String generateAndStoreInvoicePdf(
            String invoiceNumber,
            String customerName,
            LocalDateTime invoiceDate,
            List<InvoiceItemDto> items,
            Long totalAmountInMinorUnits,
            String currency,
            String hostedInvoiceUrl
    ) {
        try {
            log.info("Generating and storing invoice PDF: invoice={}", invoiceNumber);

            Map<String, Object> data = prepareInvoiceData(
                    invoiceNumber,
                    customerName,
                    invoiceDate,
                    items,
                    totalAmountInMinorUnits,
                    currency,
                    hostedInvoiceUrl
            );

            Map<String, String> metadata = new HashMap<>();
            metadata.put("invoiceId", invoiceNumber);
            metadata.put("customerName", customerName);
            metadata.put("currency", currency);

            PdfRequest request = PdfRequest.builder()
                    .templateName(INVOICE_TEMPLATE)
                    .data(data)
                    .outputFileName("invoice-" + invoiceNumber)
                    .storeFile(true)
                    .storageSubDirectory(INVOICES_STORAGE_DIR)
                    .metadata(metadata)
                    .build();

            String storagePath = pdfService.generateAndStorePdf(request);
            log.info("✓ Invoice PDF generated and stored: {}", storagePath);

            return storagePath;

        } catch (PdfGenerationException e) {
            log.error("Failed to generate and store invoice PDF: {}", invoiceNumber, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating and storing invoice PDF: {}", invoiceNumber, e);
            throw new PdfGenerationException(
                    "Invoice PDF generation and storage failed: " + e.getMessage(),
                    INVOICE_TEMPLATE,
                    "invoice=" + invoiceNumber,
                    e
            );
        }
    }

    /**
     * Prepares invoice data map for template rendering.
     * Transforms domain objects into presentation data.
     *
     * @return map of data ready to inject into template
     */
    private Map<String, Object> prepareInvoiceData(
            String invoiceNumber,
            String customerName,
            LocalDateTime invoiceDate,
            List<InvoiceItemDto> items,
            Long totalAmountInMinorUnits,
            String currency,
            String hostedInvoiceUrl
    ) {
        Map<String, Object> data = new HashMap<>();

        // Invoice metadata
        data.put("invoiceNumber", invoiceNumber);
        data.put("customerName", customerName);
        data.put("invoiceDate", invoiceDate);
        data.put("currency", currency);
        data.put("currencySymbol", getCurrencySymbol(currency));

        // Invoice amounts
        data.put("amountPaid", totalAmountInMinorUnits);
        data.put("items", items);

        // Links
        if (hostedInvoiceUrl != null && !hostedInvoiceUrl.isBlank()) {
            data.put("hostedInvoiceUrl", hostedInvoiceUrl);
        }

        return data;
    }

    /**
     * Resolves currency symbol from ISO currency code.
     * Falls back to currency code if symbol not found.
     * Supports common ISO 4217 currency codes.
     */
    private String getCurrencySymbol(String currency) {
        // Comprehensive symbol map for common currencies
        Map<String, String> symbols = Map.ofEntries(
                Map.entry("USD", "$"),
                Map.entry("EUR", "€"),
                Map.entry("GBP", "£"),
                Map.entry("INR", "₹"),
                Map.entry("CAD", "C$"),
                Map.entry("AUD", "A$"),
                Map.entry("JPY", "¥"),
                Map.entry("CHF", "Fr"),
                Map.entry("CNY", "¥"),
                Map.entry("SGD", "S$"),
                Map.entry("HKD", "HK$"),
                Map.entry("NZD", "NZ$"),
                Map.entry("ZAR", "R"),
                Map.entry("AED", "د.إ")
        );

        return symbols.getOrDefault(currency.toUpperCase(), currency.toUpperCase());
    }

    /**
     * DTO for invoice line items to transport to template.
     */
    public record InvoiceItemDto(
            String description,
            Integer quantity,
            Long pricePerUnit, // in minor units
            Long total        // in minor units
    ) {
    }
}



