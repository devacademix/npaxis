package com.digitalearn.npaxis.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for handling file storage operations.
 */
public interface StorageService {

    /**
     * Stores a file in the configured storage provider.
     *
     * @param file the file to store
     * @param subDirectory the subdirectory within the storage (e.g., "licenses", "profiles")
     * @param identifier a unique identifier for the filename (e.g., userId)
     * @return the URL or path of the stored file
     */
    String storeFile(MultipartFile file, String subDirectory, String identifier);

    /**
     * Deletes a file from the configured storage provider.
     *
     * @param fileUrl the URL or path of the file to delete
     */
    void deleteFile(String fileUrl);

    /**
     * Retrieves a file as a resource.
     *
     * @param fileUrl the URL or path of the file to retrieve
     * @return the file resource
     */
    Resource loadFileAsResource(String fileUrl);
}
