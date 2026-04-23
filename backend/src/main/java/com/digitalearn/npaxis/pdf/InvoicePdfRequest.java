package com.digitalearn.npaxis.pdf;

/**
 * Request DTO for invoice PDF generation.
 * <p>
 * Encapsulates all data needed to generate an invoice PDF.
 * Designed to be passed between services with clean separation of concerns.
 * <p>
 * PRODUCTION DESIGN:
 * - Immutable record for thread-safety
 * - Flexible context map for template rendering
 * - No business logic, data transfer only
 */
public record InvoicePdfRequest(
        String invoiceId,          // Unique identifier for idempotency
        String invoiceNumber,      // Invoice number for display
        String customerName,       // Bill-to name
        String invoiceDate,        // Formatted invoice date
        java.util.Map<String, Object> templateContext  // All template variables
) {
    /**
     * Builder for convenience
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String invoiceId;
        private String invoiceNumber;
        private String customerName;
        private String invoiceDate;
        private java.util.Map<String, Object> templateContext = new java.util.HashMap<>();

        public Builder invoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
            return this;
        }

        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder invoiceDate(String invoiceDate) {
            this.invoiceDate = invoiceDate;
            return this;
        }

        public Builder templateContext(java.util.Map<String, Object> context) {
            this.templateContext = new java.util.HashMap<>(context);
            return this;
        }

        public Builder addContextVariable(String key, Object value) {
            this.templateContext.put(key, value);
            return this;
        }

        public InvoicePdfRequest build() {
            if (invoiceId == null || invoiceNumber == null || customerName == null || invoiceDate == null) {
                throw new IllegalArgumentException("Required fields: invoiceId, invoiceNumber, customerName, invoiceDate");
            }
            return new InvoicePdfRequest(invoiceId, invoiceNumber, customerName, invoiceDate, templateContext);
        }
    }
}

