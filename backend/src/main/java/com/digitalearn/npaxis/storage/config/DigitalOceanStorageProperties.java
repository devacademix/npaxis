package com.digitalearn.npaxis.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for DigitalOcean Spaces storage.
 * Binds to 'digitalocean.spaces' prefix in application.yml.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "digitalocean.spaces")
public class DigitalOceanStorageProperties {

    /**
     * The DigitalOcean Spaces endpoint URL (e.g., https://nyc3.digitaloceanspaces.com)
     */
    private String endpoint;

    /**
     * The DigitalOcean region (e.g., nyc3)
     */
    private String region;

    /**
     * AWS access key for S3-compatible authentication
     */
    private String accessKey;

    /**
     * AWS secret key for S3-compatible authentication
     */
    private String secretKey;

    /**
     * The bucket name to use for storage
     */
    private String bucketName;

    /**
     * The CDN endpoint for public access to files (e.g., https://cdn.example.com)
     */
    private String cdnEndpoint;

    /**
     * Maximum file size in bytes (default: 10 MB)
     */
    private long maxFileSizeBytes = 10485760L;

    /**
     * Duration for pre-signed URL expiry (e.g., PT1H for 1 hour)
     */
    private Duration presignedUrlExpiry = Duration.ofHours(1);

    /**
     * List of allowed CORS origins for the bucket
     */
    private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:5173");
}

