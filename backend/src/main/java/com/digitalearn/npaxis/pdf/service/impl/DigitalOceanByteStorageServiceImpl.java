package com.digitalearn.npaxis.pdf.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.digitalearn.npaxis.exceptions.StorageException;
import com.digitalearn.npaxis.exceptions.StorageFileNotFoundException;
import com.digitalearn.npaxis.pdf.service.ByteStorageService;
import com.digitalearn.npaxis.storage.config.DigitalOceanStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

/**
 * DigitalOcean Spaces implementation of ByteStorageService.
 * Stores generated PDF bytes to DigitalOcean Spaces (S3-compatible API).
 * <p>
 * Active only in 'prod' profile and when DigitalOcean endpoint is configured.
 * Handles PDF storage with:
 * - S3-compatible upload using TransferManager
 * - Public read access via CDN
 * - Presigned URL generation
 * - Content type metadata
 * <p>
 * Complements DigitalOceanStorageServiceImpl which handles MultipartFile uploads.
 */
@Slf4j
@Service
@Profile("prod")
@ConditionalOnProperty(prefix = "digitalocean.spaces", name = "endpoint")
@RequiredArgsConstructor
public class DigitalOceanByteStorageServiceImpl implements ByteStorageService {

    private final AmazonS3 amazonS3;
    private final DigitalOceanStorageProperties properties;

    @Override
    public String storeBytes(byte[] content, String subDirectory, String fileName, String contentType) {
        validateInput(content, subDirectory, fileName);

        try {
            // 1. Construct safe object key
            String objectKey = buildObjectKey(subDirectory, fileName);
            log.debug("Constructed object key for PDF: {}", objectKey);

            // 2. Create InputStream from bytes
            InputStream inputStream = new ByteArrayInputStream(content);

            // 3. Build ObjectMetadata with content type and length
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType != null ? contentType : "application/pdf");
            metadata.setContentLength(content.length);
            metadata.addUserMetadata("x-app-uploaded-for", "pdf-generation");

            // 4. Upload to DigitalOcean Spaces
            amazonS3.putObject(
                    properties.getBucketName(),
                    objectKey,
                    inputStream,
                    metadata
            );

            // 5. Set public read access
            amazonS3.setObjectAcl(properties.getBucketName(), objectKey, CannedAccessControlList.PublicRead);

            // 6. Build CDN URL for public access
            String cdnUrl = String.format("%s/%s", properties.getCdnEndpoint(), objectKey);

            // 7. Log successful storage
            log.info("PDF stored to DigitalOcean Spaces: key={}, size={} bytes, type={}, cdnUrl={}",
                    objectKey, content.length, contentType, cdnUrl);

            return cdnUrl;

        } catch (AmazonServiceException e) {
            log.error("AWS service error while storing PDF: fileName={}, error={}", fileName, e.getMessage(), e);
            throw new StorageException("AWS service error while storing PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while storing PDF bytes: fileName={}, error={}", fileName, e.getMessage(), e);
            throw new StorageException("Failed to store PDF bytes: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            log.warn("Attempted to delete PDF with null or empty URL");
            throw new StorageException("PDF URL cannot be null or empty");
        }

        try {
            // 1. Extract object key from URL
            String objectKey = extractObjectKeyFromUrl(filePath);

            // 2. Validate extracted key
            if (!StringUtils.hasText(objectKey)) {
                log.warn("Could not extract object key from PDF URL: {}", filePath);
                throw new StorageException("Invalid PDF URL format");
            }

            // 3. Delete from S3
            amazonS3.deleteObject(properties.getBucketName(), objectKey);

            // 4. Log successful deletion
            log.info("PDF deleted from DigitalOcean Spaces: key={}", objectKey);

        } catch (AmazonServiceException e) {
            // Idempotency: treat 404 as success
            if (e.getStatusCode() == 404) {
                log.warn("PDF not found (404) during deletion, treating as success (idempotent): url={}", filePath);
                return;
            }
            log.error("AWS service error while deleting PDF: url={}, error={}", filePath, e.getMessage(), e);
            throw new StorageException("Failed to delete PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting PDF: url={}, error={}", filePath, e.getMessage(), e);
            throw new StorageException("Failed to delete PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            log.warn("Attempted to load PDF with null or empty URL");
            throw new StorageFileNotFoundException("PDF URL cannot be null or empty");
        }

        try {
            // 1. Extract object key from URL
            String objectKey = extractObjectKeyFromUrl(filePath);

            // 2. Validate extracted key
            if (!StringUtils.hasText(objectKey)) {
                log.warn("Could not extract object key from PDF URL: {}", filePath);
                throw new StorageFileNotFoundException("Invalid PDF URL format");
            }

            // 3. Check if object exists
            if (!amazonS3.doesObjectExist(properties.getBucketName(), objectKey)) {
                log.warn("PDF not found in DigitalOcean Spaces: key={}", objectKey);
                throw new StorageFileNotFoundException("PDF not found: " + objectKey);
            }

            // 4. Retrieve object from S3
            S3Object s3Object = amazonS3.getObject(properties.getBucketName(), objectKey);

            // 5. Log successful retrieval
            log.debug("PDF loaded as resource from DigitalOcean Spaces: key={}", objectKey);

            // 6. Return wrapped InputStream
            return new InputStreamResource(s3Object.getObjectContent());

        } catch (StorageFileNotFoundException e) {
            throw e;
        } catch (AmazonServiceException e) {
            log.error("AWS service error while loading PDF: url={}, error={}", filePath, e.getMessage(), e);
            throw new StorageFileNotFoundException("Failed to load PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while loading PDF: url={}, error={}", filePath, e.getMessage(), e);
            throw new StorageFileNotFoundException("Failed to load PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a pre-signed URL for temporary access to a PDF without authentication.
     * The URL expires after the configured duration.
     *
     * @param objectKey the S3 object key
     * @param expiry    optional expiry duration; uses configured default if null
     * @return the pre-signed URL as a String
     */
    public String generatePresignedUrl(String objectKey, Duration expiry) {
        if (!StringUtils.hasText(objectKey)) {
            throw new StorageException("PDF object key cannot be null or empty");
        }

        try {
            Duration effectiveExpiry = expiry != null ? expiry : properties.getPresignedUrlExpiry();
            Date expiryDate = new Date(System.currentTimeMillis() + effectiveExpiry.toMillis());

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    properties.getBucketName(),
                    objectKey
            ).withExpiration(expiryDate);

            URL presignedUrl = amazonS3.generatePresignedUrl(request);

            log.debug("Presigned URL generated for PDF: objectKey={}, expiry={}", objectKey, effectiveExpiry);

            return presignedUrl.toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL for PDF: objectKey={}, error={}", objectKey, e.getMessage(), e);
            throw new StorageException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    /**
     * Constructs a sanitized, collision-free object key for PDF storage in S3.
     * Format: {subDirectory}/{uuid}_{sanitizedFileName}
     * Example: invoices/3fa85f64-uuid_invoice-INV-12345.pdf
     *
     * @param subDirectory the target subdirectory (e.g., "invoices", "certificates")
     * @param fileName     the file name (should include extension)
     * @return the constructed object key
     */
    private String buildObjectKey(String subDirectory, String fileName) {
        // Add UUID for collision prevention
        String uuid = UUID.randomUUID().toString();

        // Sanitize fileName: remove path traversal characters
        String sanitized = fileName
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("^_+|(_+)$", "");

        // Build the key
        return String.format("%s/%s_%s", subDirectory, uuid, sanitized);
    }

    /**
     * Extracts the object key from a full S3 or CDN URL.
     * Handles both forms:
     * - https://cdn.example.com/invoices/file.pdf (returns "invoices/file.pdf")
     * - https://nyc3.digitaloceanspaces.com/bucket-name/invoices/file.pdf (returns "invoices/file.pdf")
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
            log.warn("Failed to parse PDF URL: {}, error={}", urlString, e.getMessage());
            return "";
        }
    }

    /**
     * Validates input parameters.
     */
    private void validateInput(byte[] content, String subDirectory, String fileName) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("PDF content cannot be null or empty");
        }

        if (!StringUtils.hasText(subDirectory)) {
            throw new IllegalArgumentException("PDF subdirectory cannot be empty");
        }

        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("PDF file name cannot be empty");
        }

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name: path separators not allowed");
        }
    }
}

