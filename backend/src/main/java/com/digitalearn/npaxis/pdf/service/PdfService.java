package com.digitalearn.npaxis.pdf.service;

import com.digitalearn.npaxis.pdf.exception.PdfGenerationException;

/**
 * Service interface for PDF generation.
 * Provides two main operations: generating and returning bytes, or generating and storing PDFs.
 * <p>
 * Implementations must be:
 * - Thread-safe
 * - Stateless
 * - Transaction-aware (if persistence is involved)
 */
public interface PdfService {

    /**
     * Generates a PDF from a template and returns the raw bytes.
     * No storage is performed.
     *
     * @param request the PDF generation request containing template name and data
     * @return PDF content as bytes
     * @throws PdfGenerationException   if generation fails at any stage
     * @throws IllegalArgumentException if request is invalid
     */
    byte[] generatePdf(PdfRequest request);

    /**
     * Generates a PDF and stores it using the configured storage service.
     * Returns the storage path/URL for later retrieval.
     * <p>
     * Prerequisites:
     * - request.storeFile must be true
     * - request.storageSubDirectory must be set
     * - StorageService must be available
     *
     * @param request the PDF generation request with storage details
     * @return storage path or URL where PDF is stored
     * @throws PdfGenerationException   if generation or storage fails
     * @throws IllegalArgumentException if request is invalid or missing required fields for storage
     */
    String generateAndStorePdf(PdfRequest request);
}

