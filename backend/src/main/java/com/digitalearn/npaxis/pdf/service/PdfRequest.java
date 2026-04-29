package com.digitalearn.npaxis.pdf.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic PDF generation request model.
 * Encapsulates all data needed to produce a PDF document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PdfRequest {

    /**
     * Name of the Thymeleaf template (e.g., "invoice", "certificate")
     */
    private String templateName;

    /**
     * Dynamic data to inject into the template
     */
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    /**
     * Output file name without extension (e.g., "invoice-2024-001")
     */
    private String outputFileName;

    /**
     * Whether to persist PDF to storage (true = store, false = return bytes only)
     */
    @Builder.Default
    private boolean storeFile = false;

    /**
     * Storage subdirectory (e.g., "invoices", "certificates") - used if storeFile is true
     */
    private String storageSubDirectory;

    /**
     * Optional metadata for tracking/auditing purposes
     */
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    /**
     * Add data entry to the model
     */
    public void putData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
    }

    /**
     * Add metadata entry
     */
    public void putMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
}

