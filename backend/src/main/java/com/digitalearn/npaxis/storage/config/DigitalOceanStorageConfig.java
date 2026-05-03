package com.digitalearn.npaxis.storage.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for DigitalOcean Spaces S3-compatible client.
 * Instantiates beans for AmazonS3 and TransferManager used by DigitalOceanStorageServiceImpl.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "digitalocean.spaces", name = "endpoint")
public class DigitalOceanStorageConfig {

    private TransferManager transferManager;

    /**
     * Creates and configures the AmazonS3 client for DigitalOcean Spaces.
     *
     * @param properties the DigitalOcean Spaces properties
     * @return configured AmazonS3 client
     */
    @Bean
    public AmazonS3 amazonS3(DigitalOceanStorageProperties properties) {
        log.info("Initializing AmazonS3 client for DigitalOcean Spaces: endpoint={}, region={}",
                properties.getEndpoint(), properties.getRegion());

        // Create credentials provider with access and secret keys
        BasicAWSCredentials credentials = new BasicAWSCredentials(
                properties.getAccessKey(),
                properties.getSecretKey()
        );

        // Configure client to use path-style access and AWSS3V4SignerType for DigitalOcean
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setSignerOverride("AWSS3V4SignerType");

        // Configure endpoint and build client
        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(properties.getEndpoint(), properties.getRegion());

        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(endpointConfig)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfig)
                .build();
    }

    /**
     * Creates and configures the TransferManager for multipart uploads.
     * This bean manages concurrent uploads and large file transfers.
     *
     * @param amazonS3 the AmazonS3 client
     * @return configured TransferManager
     */
    @Bean
    public TransferManager transferManager(AmazonS3 amazonS3) {
        log.info("Initializing TransferManager for multipart uploads");

        this.transferManager = TransferManagerBuilder.standard()
                .withS3Client(amazonS3)
                .withMultipartUploadThreshold(5L * 1024 * 1024)  // 5 MB threshold
                .withMinimumUploadPartSize(5L * 1024 * 1024)      // 5 MB per part
                .withShutDownThreadPools(true)
                .build();

        return transferManager;
    }

    /**
     * Gracefully shut down the TransferManager on application shutdown.
     * This ensures all pending transfers are completed and resources are freed.
     * Note: This method can be called via reflection or Spring lifecycle hooks.
     */
    public void shutdownTransferManager() {
        if (transferManager != null) {
            log.info("Shutting down TransferManager");
            transferManager.shutdownNow(false);
        }
    }
}



