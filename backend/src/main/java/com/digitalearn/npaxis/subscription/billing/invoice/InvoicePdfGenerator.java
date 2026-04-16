package com.digitalearn.npaxis.subscription.billing.invoice;

import com.digitalearn.npaxis.subscription.core.PreceptorSubscription;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoicePdfGenerator {

    private static final String INVOICES_DIR = "invoices";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Generate invoice PDF file from HTML content
     *
     * @param subscription the preceptor subscription
     * @param eventType    the subscription event type (CREATED, UPGRADED, CANCELED)
     * @return File object pointing to the generated PDF
     */
    public File generateSubscriptionInvoice(PreceptorSubscription subscription, String eventType) {
        try {
            // Create invoices directory if it doesn't exist
            Path invoiceDirPath = Paths.get(INVOICES_DIR);
            Files.createDirectories(invoiceDirPath);

            // Generate HTML content
            String htmlContent = buildInvoiceHtml(subscription, eventType);

            // Create PDF file
            String fileName = generateFileName(subscription.getPreceptor().getUserId(), eventType);
            File pdfFile = new File(invoiceDirPath.toFile(), fileName);

            // Convert HTML to PDF
            convertHtmlToPdf(htmlContent, pdfFile);

            log.info("Invoice PDF generated successfully: {}", pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (IOException e) {
            log.error("Error generating invoice PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    /**
     * Convert HTML content to PDF using iText 2.1.7
     */
    private void convertHtmlToPdf(String htmlContent, File outputFile) throws IOException {
        Document document = new Document();
        try (FileOutputStream os = new FileOutputStream(outputFile)) {
            PdfWriter.getInstance(document, os);
            document.open();

            // Parse HTML and add to document
            HTMLWorker htmlWorker = new HTMLWorker(document);
            htmlWorker.parse(new StringReader(htmlContent));

            document.close();
            log.debug("HTML successfully converted to PDF: {}", outputFile.getAbsolutePath());
        } catch (DocumentException | IOException e) {
            log.error("Error converting HTML to PDF: {}", e.getMessage(), e);
            if (document.isOpen()) {
                document.close();
            }
            throw new IOException("Failed to convert HTML to PDF", e);
        }
    }

    /**
     * Build HTML content for invoice
     */
    private String buildInvoiceHtml(PreceptorSubscription subscription, String eventType) {
        double amountInCurrency = subscription.getPrice().getAmountInMinorUnits() / 100.0;
        String planName = subscription.getPlan().getName();
        String preceptorName = subscription.getPreceptor().getName();
        String preceptorEmail = subscription.getPreceptor().getEmail();
        String billingInterval = subscription.getPrice().getBillingInterval().toString().toLowerCase();
        String currency = subscription.getPrice().getCurrency().toUpperCase();

        LocalDateTime now = LocalDateTime.now();
        String invoiceDate = now.format(DATE_FORMATTER);
        String invoiceNumber = generateInvoiceNumber();

        String eventTitle = switch (eventType) {
            case "SUBSCRIPTION_CREATED" -> "Subscription Invoice";
            case "SUBSCRIPTION_UPGRADED" -> "Plan Upgrade Invoice";
            case "SUBSCRIPTION_CANCELED" -> "Final Invoice";
            default -> "Invoice";
        };

        String firstBillingIntervalChar = billingInterval.substring(0, 1).toUpperCase();
        String restBillingInterval = billingInterval.substring(1);

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; background-color: #fff; }\n" +
                "        .container { max-width: 800px; margin: 0 auto; padding: 40px; }\n" +
                "        .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 40px; border-bottom: 2px solid #0A2540; padding-bottom: 20px; }\n" +
                "        .company-info h1 { color: #0A2540; font-size: 28px; margin-bottom: 5px; }\n" +
                "        .company-info p { color: #666; font-size: 12px; }\n" +
                "        .invoice-title { text-align: right; }\n" +
                "        .invoice-title h2 { color: #0A2540; font-size: 20px; margin-bottom: 10px; }\n" +
                "        .invoice-meta { font-size: 12px; color: #666; margin-bottom: 5px; }\n" +
                "        .section { margin-bottom: 30px; }\n" +
                "        .section-title { color: #0A2540; font-size: 12px; font-weight: bold; text-transform: uppercase; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 8px; }\n" +
                "        .section-content { font-size: 13px; line-height: 1.6; }\n" +
                "        .two-column { display: flex; gap: 40px; }\n" +
                "        .column { flex: 1; }\n" +
                "        .items-table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n" +
                "        .items-table th { background-color: #f5f5f5; color: #0A2540; text-align: left; padding: 12px; font-size: 12px; font-weight: bold; border-bottom: 2px solid #0A2540; }\n" +
                "        .items-table td { padding: 12px; font-size: 13px; border-bottom: 1px solid #eee; }\n" +
                "        .items-table tr:last-child td { border-bottom: none; }\n" +
                "        .items-table .text-right { text-align: right; }\n" +
                "        .summary-table { width: 100%; margin-top: 20px; }\n" +
                "        .summary-table tr { border: none; }\n" +
                "        .summary-table td { padding: 10px 0; font-size: 13px; }\n" +
                "        .summary-table .label { text-align: right; width: 70%; color: #666; }\n" +
                "        .summary-table .value { text-align: right; width: 30%; font-weight: bold; color: #0A2540; }\n" +
                "        .summary-table .total-row { border-top: 2px solid #0A2540; border-bottom: 2px solid #0A2540; background-color: #f5f5f5; font-size: 16px; }\n" +
                "        .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; font-size: 11px; color: #999; }\n" +
                "        .note { background-color: #f0f8ff; padding: 15px; border-left: 4px solid #0A2540; margin: 20px 0; font-size: 12px; color: #333; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <div class=\"company-info\">\n" +
                "                <h1>NPaxis</h1>\n" +
                "                <p>Digitalearn Solution Pvt. Ltd.</p>\n" +
                "                <p>Professional Preceptor Network</p>\n" +
                "            </div>\n" +
                "            <div class=\"invoice-title\">\n" +
                "                <h2>" + eventTitle + "</h2>\n" +
                "                <div class=\"invoice-meta\">Invoice #: " + invoiceNumber + "</div>\n" +
                "                <div class=\"invoice-meta\">Date: " + invoiceDate + "</div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div class=\"two-column\">\n" +
                "            <div class=\"column\">\n" +
                "                <div class=\"section\">\n" +
                "                    <div class=\"section-title\">Bill To</div>\n" +
                "                    <div class=\"section-content\">\n" +
                "                        <strong>" + preceptorName + "</strong><br/>\n" +
                "                        Email: " + preceptorEmail + "<br/>\n" +
                "                        Subscriber ID: " + subscription.getPreceptor().getUserId() + "\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div class=\"column\">\n" +
                "                <div class=\"section\">\n" +
                "                    <div class=\"section-title\">Subscription Details</div>\n" +
                "                    <div class=\"section-content\">\n" +
                "                        Plan: <strong>" + planName + "</strong><br/>\n" +
                "                        Billing Cycle: <strong>" + firstBillingIntervalChar + restBillingInterval + "</strong><br/>\n" +
                "                        Status: <strong>" + subscription.getStatus() + "</strong>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div class=\"section\">\n" +
                "            <div class=\"section-title\">Billing Summary</div>\n" +
                "            <table class=\"items-table\">\n" +
                "                <thead>\n" +
                "                    <tr>\n" +
                "                        <th>Description</th>\n" +
                "                        <th style=\"width: 15%;\">Quantity</th>\n" +
                "                        <th style=\"width: 20%;\">Unit Price</th>\n" +
                "                        <th style=\"width: 20%;\" class=\"text-right\">Amount</th>\n" +
                "                    </tr>\n" +
                "                </thead>\n" +
                "                <tbody>\n" +
                "                    <tr>\n" +
                "                        <td>" + planName + " Subscription - " + billingInterval + "</td>\n" +
                "                        <td>1</td>\n" +
                "                        <td>$" + String.format("%.2f", amountInCurrency) + " " + currency + "</td>\n" +
                "                        <td class=\"text-right\"><strong>$" + String.format("%.2f", amountInCurrency) + " " + currency + "</strong></td>\n" +
                "                    </tr>\n" +
                "                </tbody>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "        <div class=\"section\">\n" +
                "            <table class=\"summary-table\">\n" +
                "                <tr>\n" +
                "                    <td class=\"label\">Subtotal:</td>\n" +
                "                    <td class=\"value\">$" + String.format("%.2f", amountInCurrency) + " " + currency + "</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td class=\"label\">Tax (0%):</td>\n" +
                "                    <td class=\"value\">$0.00</td>\n" +
                "                </tr>\n" +
                "                <tr class=\"total-row\">\n" +
                "                    <td class=\"label\"><strong>Total Due:</strong></td>\n" +
                "                    <td class=\"value\"><strong>$" + String.format("%.2f", amountInCurrency) + " " + currency + "</strong></td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </div>\n" +
                "        <div class=\"note\">\n" +
                "            <strong>Note:</strong> This is an automated invoice generated on subscription " + eventType.toLowerCase().replace("_", " ") + ". \n" +
                "            For questions about your subscription, please contact our support team.\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2026 Digitalearn Solution Pvt. Ltd. All rights reserved.</p>\n" +
                "            <p>Thank you for using NPaxis!</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + timestamp + "-" + randomPart;
    }

    /**
     * Generate unique file name
     */
    private String generateFileName(Long userId, String eventType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "invoice-" + userId + "-" + eventType.toLowerCase() + "-" + timestamp + ".pdf";
    }
}





