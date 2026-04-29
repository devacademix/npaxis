package com.digitalearn.npaxis.pdf.exception;

/**
 * Custom exception for PDF generation failures.
 * Wraps underlying rendering, template, and IO errors.
 */
public class PdfGenerationException extends RuntimeException {

    private final String templateName;
    private final String context;

    /**
     * Create a PDF generation exception with a message.
     *
     * @param message      descriptive error message
     * @param templateName name of the template that failed
     * @param context      additional context information
     */
    public PdfGenerationException(String message, String templateName, String context) {
        super(message);
        this.templateName = templateName;
        this.context = context;
    }

    /**
     * Create a PDF generation exception with a cause.
     *
     * @param message      descriptive error message
     * @param templateName name of the template that failed
     * @param context      additional context information
     * @param cause        underlying exception
     */
    public PdfGenerationException(String message, String templateName, String context, Throwable cause) {
        super(message, cause);
        this.templateName = templateName;
        this.context = context;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String toString() {
        return String.format(
                "PdfGenerationException: %s (template: %s, context: %s)",
                getMessage(),
                templateName,
                context
        );
    }
}

