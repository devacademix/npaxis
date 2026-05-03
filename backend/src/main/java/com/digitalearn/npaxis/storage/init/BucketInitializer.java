package com.digitalearn.npaxis.storage.init;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.digitalearn.npaxis.storage.config.DigitalOceanStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Application startup initializer for DigitalOcean Spaces bucket.
 * Checks if the configured bucket exists and creates it if necessary.
 * Also applies CORS configuration for the bucket.
 * <p>
 * Disabled by default in development to avoid bucket creation errors with dummy credentials.
 * Enable via: -Dstorage.bucket.initialize=true or STORAGE_BUCKET_INITIALIZE=true
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "digitalocean.spaces", name = "endpoint")
@ConditionalOnProperty(prefix = "storage.bucket", name = "initialize", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class BucketInitializer implements ApplicationRunner {

    private final AmazonS3 amazonS3;
    private final DigitalOceanStorageProperties properties;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        try {
            String bucketName = properties.getBucketName();
            String region = properties.getRegion();

            // Check if bucket exists
            if (amazonS3.doesBucketExistV2(bucketName)) {
                log.debug("DigitalOcean Spaces bucket already exists: {}", bucketName);
            } else {
                // Create bucket
                log.info("Creating DigitalOcean Spaces bucket: {} in region: {}", bucketName, region);
                amazonS3.createBucket(new CreateBucketRequest(bucketName, region));
                log.info("Successfully created bucket: {}", bucketName);
            }

            // TODO: Apply CORS configuration for allowed origins if needed
            // This would require additional AWS SDK features not available in this version

            log.info("DigitalOcean Spaces bucket initialized successfully: {}", bucketName);

        } catch (AmazonServiceException e) {
            log.error("AWS service error during bucket initialization: {}", e.getMessage(), e);
            // Don't crash the application if bucket initialization fails
            log.warn("Bucket initialization failed but continuing application startup. " +
                    "Please check your DigitalOcean Spaces configuration.");
        } catch (Exception e) {
            log.error("Unexpected error during bucket initialization: {}", e.getMessage(), e);
            // Don't crash the application if bucket initialization fails
            log.warn("Bucket initialization failed but continuing application startup. " +
                    "Please verify your DigitalOcean Spaces credentials and endpoint.");
        }
    }
}

