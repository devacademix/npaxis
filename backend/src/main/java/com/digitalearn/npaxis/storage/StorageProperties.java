package com.digitalearn.npaxis.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "npaxis.storage")
public class StorageProperties {

    /**
     * The root directory where files will be stored for local service.
     */
    private String localDirectory = "uploads";

    /**
     * Allowed file extensions.
     */
    private List<String> allowedExtensions = List.of("pdf", "png", "jpg", "jpeg");

    /**
     * Cloud-specific configurations (e.g., S3 bucket, region, credentials).
     */
    private CloudProperties cloud = new CloudProperties();

    @Data
    public static class CloudProperties {
        private String bucketName;
        private String region;
        private String accessKey;
        private String secretKey;
    }
}
