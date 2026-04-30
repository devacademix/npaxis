package com.digitalearn.npaxis.pdf.service;

import org.springframework.core.io.Resource;

/**
 * Extended storage service interface for handling byte arrays.
 * Complements the existing StorageService which works with MultipartFile.
 * <p>
 * This interface is used by PDF module for storing generated PDF bytes.
 * Implementations should support both local and cloud storage backends.
 */
public interface ByteStorageService {

    /**
     * Stores a byte array to the configured storage.
     *
     * @param content      the file content as bytes
     * @param subDirectory the subdirectory within storage (e.g., "invoices", "certificates")
     * @param fileName     the file name (should already include extension)
     * @param contentType  the MIME type (e.g., "application/pdf")
     * @return the stored file URL or path
     */
    String storeBytes(byte[] content, String subDirectory, String fileName, String contentType);

    /**
     * Deletes a stored file.
     *
     * @param filePath the URL or path of the file to delete
     */
    void deleteFile(String filePath);

    /**
     * Loads a stored file as a resource.
     *
     * @param filePath the URL or path of the file
     * @return the file resource
     */
    Resource loadFileAsResource(String filePath);
}

