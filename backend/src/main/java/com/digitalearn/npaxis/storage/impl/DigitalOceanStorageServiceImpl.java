package com.digitalearn.npaxis.storage.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.digitalearn.npaxis.exceptions.*;
import com.digitalearn.npaxis.storage.StorageService;
import com.digitalearn.npaxis.storage.config.DigitalOceanStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * DigitalOcean Spaces storage service implementation.
 * Handles file upload, deletion, and retrieval using S3-compatible API.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "digitalocean.spaces", name = "endpoint")
@RequiredArgsConstructor
public class DigitalOceanStorageServiceImpl implements StorageService {

    private final AmazonS3 amazonS3;
    private final TransferManager transferManager;
    private final DigitalOceanStorageProperties properties;

    // Constants
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    // Pattern to sanitize filenames: remove path traversal chars, spaces, and non-ASCII
    private static final Pattern FILENAME_SANITIZE_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");

    @Override
    public String storeFile(MultipartFile file, String subDirectory, String identifier) {
        // 1. Null and empty guard
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to store null or empty file: subDirectory={}, identifier={}", subDirectory, identifier);
            throw new StorageException("File cannot be null or empty");
        }

        final long fileSize = file.getSize();

        // 2. File size validation
        if (fileSize > properties.getMaxFileSizeBytes()) {
            log.warn("File size exceeded: size={} bytes, max={} bytes, identifier={}",
                     fileSize, properties.getMaxFileSizeBytes(), identifier);
            throw new StorageFileSizeExceededException(
                    String.format("File size %d bytes exceeds maximum allowed %d bytes",
                                  fileSize, properties.getMaxFileSizeBytes()),
                    fileSize,
                    properties.getMaxFileSizeBytes()
            );
        }

        // 3. MIME type validation
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("Unsupported file type: contentType={}, identifier={}", contentType, identifier);
            throw new StorageUnsupportedFileTypeException(
                    String.format("File type not permitted: %s", contentType),
                    contentType
            );
        }

        // 4. Filename construction
        String objectKey = buildObjectKey(subDirectory, identifier, file.getOriginalFilename());
        log.debug("Constructed object key: {}", objectKey);

        // 5. S3 PutObject using TransferManager
        try {
            InputStream inputStream = file.getInputStream();

            // Build ObjectMetadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileSize);
            metadata.addUserMetadata("x-app-uploaded-by", identifier);

            // Upload with TransferManager (supports multipart for large files)
            Upload upload = transferManager.upload(
                    properties.getBucketName(),
                    objectKey,
                    inputStream,
                    metadata
            );

            // Wait for completion
            upload.waitForCompletion();

            // Set public read access
            amazonS3.setObjectAcl(properties.getBucketName(), objectKey, CannedAccessControlList.PublicRead);

            // 6. CDN URL construction
            String cdnUrl = String.format("%s/%s", properties.getCdnEndpoint(), objectKey);

            // 7. Logging
            log.info("File stored: key={}, size={} bytes, type={}", objectKey, fileSize, contentType);

            return cdnUrl;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Upload interrupted: identifier={}, subDirectory={}, error={}", identifier, subDirectory, e.getMessage(), e);
            throw new StorageException("File upload was interrupted", e);
        } catch (IOException e) {
            log.error("Failed to read file stream: identifier={}, subDirectory={}, error={}", identifier, subDirectory, e.getMessage(), e);
            throw new StorageException("Failed to read file", e);
        } catch (AmazonServiceException e) {
            log.error("AWS service error while uploading: identifier={}, subDirectory={}, error={}", identifier, subDirectory, e.getMessage(), e);
            throw new StorageException("AWS service error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while storing file: identifier={}, subDirectory={}, error={}", identifier, subDirectory, e.getMessage(), e);
            throw new StorageException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            log.warn("Attempted to delete file with null or empty URL");
            throw new StorageException("File URL cannot be null or empty");
        }

        try {
            // 1. Key extraction
            String objectKey = extractObjectKeyFromUrl(fileUrl);

            // 2. Guard
            if (!StringUtils.hasText(objectKey)) {
                log.warn("Could not extract object key from URL: {}", fileUrl);
                throw new StorageException("Invalid file URL format");
            }

            // 3. S3 DeleteObject
            amazonS3.deleteObject(properties.getBucketName(), objectKey);

            // 5. Logging
            log.info("File deleted: key={}", objectKey);

        } catch (AmazonServiceException e) {
            // 4. Idempotency - if 404, ignore
            if (e.getStatusCode() == 404) {
                log.warn("File not found (404) during deletion, continuing (idempotent): url={}", fileUrl);
                return;
            }
            log.error("AWS service error while deleting: url={}, error={}", fileUrl, e.getMessage(), e);
            throw new StorageException("Failed to delete file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting file: url={}, error={}", fileUrl, e.getMessage(), e);
            throw new StorageException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            log.warn("Attempted to load file with null or empty URL");
            throw new StorageFileNotFoundException("File URL cannot be null or empty");
        }

        try {
            // 1. Key extraction
            String objectKey = extractObjectKeyFromUrl(fileUrl);

            if (!StringUtils.hasText(objectKey)) {
                log.warn("Could not extract object key from URL: {}", fileUrl);
                throw new StorageFileNotFoundException("Invalid file URL format");
            }

            // 2. Existence check
            if (!amazonS3.doesObjectExist(properties.getBucketName(), objectKey)) {
                log.warn("File not found in storage: key={}", objectKey);
                throw new StorageFileNotFoundException("File not found: " + objectKey);
            }

            // 3. S3 GetObject
            S3Object s3Object = amazonS3.getObject(properties.getBucketName(), objectKey);

            // 5. Logging
            log.debug("File loaded as resource: key={}", objectKey);

            // 4. Return wrapped InputStream
            return new InputStreamResource(s3Object.getObjectContent());

        } catch (StorageFileNotFoundException e) {
            throw e;
        } catch (AmazonServiceException e) {
            log.error("AWS service error while loading file: url={}, error={}", fileUrl, e.getMessage(), e);
            throw new StorageFileNotFoundException("Failed to load file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while loading file: url={}, error={}", fileUrl, e.getMessage(), e);
            throw new StorageFileNotFoundException("Failed to load file: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a pre-signed URL for temporary access to a file without authentication.
     * The URL expires after the configured duration.
     *
     * @param objectKey the S3 object key
     * @param expiry    optional expiry duration; uses configured default if null
     * @return the pre-signed URL as a String
     */
    public String generatePresignedUrl(String objectKey, Duration expiry) {
        if (!StringUtils.hasText(objectKey)) {
            throw new StorageException("Object key cannot be null or empty");
        }

        try {
            Duration effectiveExpiry = expiry != null ? expiry : properties.getPresignedUrlExpiry();
            Date expiryDate = new Date(System.currentTimeMillis() + effectiveExpiry.toMillis());

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    properties.getBucketName(),
                    objectKey
            ).withExpiration(expiryDate);

            URL presignedUrl = amazonS3.generatePresignedUrl(request);

            log.debug("Presigned URL generated for key={}, expiry={}", objectKey, effectiveExpiry);

            return presignedUrl.toString();

        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL: objectKey={}, error={}", objectKey, e.getMessage(), e);
            throw new StorageException("Failed to generate pre-signed URL: " + e.getMessage(), e);
        }
    }

    /**
     * Constructs a sanitized, collision-free object key for S3 storage.
     * Format: {subDirectory}/{identifier}_{uuid}_{sanitizedFilename}.{extension}
     * Example: profiles/user42_3fa85f64-uuid_avatar.jpg
     *
     * @param subDirectory   the target subdirectory (e.g., "licenses", "profiles")
     * @param identifier     a unique identifier (e.g., userId)
     * @param originalFilename the original filename from the uploaded file
     * @return the constructed object key
     */
    private String buildObjectKey(String subDirectory, String identifier, String originalFilename) {
        // Extract extension
        String extension = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        // Sanitize filename: remove path traversal and non-alphanumeric characters
        String sanitized = FILENAME_SANITIZE_PATTERN.matcher(originalFilename != null ? originalFilename : "file")
                .replaceAll("_");
        // Remove leading/trailing underscores
        sanitized = sanitized.replaceAll("^_+|(_+)$", "_");

        // Build the key
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%s_%s_%s.%s", subDirectory, identifier, uuid, sanitized, extension);
    }

    /**
     * Extracts the object key from a full S3 or CDN URL.
     * Handles both forms:
     * - https://cdn.example.com/profiles/file.jpg (returns "profiles/file.jpg")
     * - https://nyc3.digitaloceanspaces.com/bucket-name/profiles/file.jpg (returns "profiles/file.jpg")
     *
     * @param urlString the full URL
     * @return the extracted object key, or empty string if cannot be extracted
     */
    private String extractObjectKeyFromUrl(String urlString) {
        if (!StringUtils.hasText(urlString)) {
            return "";
        }

        try {
            URL url = new URL(urlString);
            String path = url.getPath();

            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // Case 1: CDN URL - the entire path is the object key
            if (urlString.contains(properties.getCdnEndpoint())) {
                return path;
            }

            // Case 2: Raw S3 URL - format is "/bucket-name/object-key"
            // Remove bucket name from the beginning
            String bucketPrefix = properties.getBucketName() + "/";
            if (path.startsWith(bucketPrefix)) {
                return path.substring(bucketPrefix.length());
            }

            // Fallback: just return the path as-is
            return path;

        } catch (Exception e) {
            log.warn("Failed to parse URL: {}, error={}", urlString, e.getMessage());
            return "";
        }
    }
}



