package com.digitalearn.npaxis.pdf.service;

import com.digitalearn.npaxis.pdf.exception.PdfGenerationException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Utility class for PDF-related operations.
 * Provides helper methods for safe byte stream handling, naming, and validation.
 */
@Slf4j
public class PdfUtils {

    private PdfUtils() {
        // Private constructor - utility class
    }

    /**
     * Validates that template name follows naming conventions.
     * Template names should be alphanumeric with hyphens, no slashes or special characters.
     *
     * @param templateName the template name to validate
     * @throws IllegalArgumentException if template name is invalid
     */
    public static void validateTemplateName(String templateName) {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }

        if (templateName.contains("/") || templateName.contains("\\")) {
            throw new IllegalArgumentException("Template name cannot contain path separators");
        }

        if (!templateName.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Template name must contain only alphanumeric characters, hyphens, or underscores");
        }
    }

    /**
     * Validates that output file name is safe and follows conventions.
     *
     * @param outputFileName the file name to validate
     * @throws IllegalArgumentException if file name is invalid
     */
    public static void validateOutputFileName(String outputFileName) {
        if (outputFileName == null || outputFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Output file name cannot be null or empty");
        }

        if (outputFileName.contains("/") || outputFileName.contains("\\")
                || outputFileName.contains("..") || outputFileName.contains(":")) {
            throw new IllegalArgumentException("Output file name contains invalid characters");
        }
    }

    /**
     * Generates a safe PDF file name with UUID suffix to ensure uniqueness.
     *
     * @param baseName the base file name (without extension)
     * @return generated file name with UUID (e.g., "invoice-001-a1b2c3d4")
     */
    public static String generateSafeFileName(String baseName) {
        validateOutputFileName(baseName);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return baseName + "-" + uuid + ".pdf";
    }

    /**
     * Validates that PDF bytes are reasonable in size.
     * Empty PDFs or suspiciously large PDFs are flagged.
     *
     * @param pdfBytes the PDF content as bytes
     * @throws IllegalArgumentException if PDF is empty or exceeds size limits
     */
    public static void validatePdfSize(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("Generated PDF is empty");
        }

        long maxSizeBytes = 50 * 1024 * 1024; // 50 MB limit
        if (pdfBytes.length > maxSizeBytes) {
            throw new IllegalArgumentException(
                    "Generated PDF exceeds maximum size limit: " + pdfBytes.length + " bytes"
            );
        }
    }

    /**
     * Safely closes a ByteArrayOutputStream and logs any errors.
     *
     * @param stream the stream to close
     */
    public static void closeStream(ByteArrayOutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                log.warn("Error closing ByteArrayOutputStream", e);
            }
        }
    }

    /**
     * Converts HTML string to UTF-8 bytes, handling encoding safely.
     *
     * @param html the HTML content
     * @return UTF-8 encoded bytes
     * @throws PdfGenerationException if encoding fails
     */
    public static byte[] htmlToBytes(String html) {
        try {
            return html.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new PdfGenerationException(
                    "Failed to encode HTML to UTF-8 bytes",
                    "encoding",
                    e.getMessage(),
                    e
            );
        }
    }

    /**
     * Safely extracts metadata value with null-safety.
     *
     * @param data         the data map
     * @param key          the key to extract
     * @param defaultValue the default value if key not found or null
     * @return the metadata value or default
     */
    public static String getMetadataValue(java.util.Map<String, ?> data, String key, String defaultValue) {
        if (data == null) {
            return defaultValue;
        }

        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}

