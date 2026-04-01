package com.digitalearn.npaxis.storage.impl;

import com.digitalearn.npaxis.storage.StorageProperties;
import com.digitalearn.npaxis.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class CloudStorageService implements StorageService {

    private final StorageProperties properties;

    @Override
    public String storeFile(MultipartFile file, String subDirectory, String identifier) {
        log.info("CloudStorageService: storing file to cloud bucket {}/{}", 
                properties.getCloud().getBucketName(), subDirectory);
        
        // Mock cloud storage logic. In a real scenario, upload a file to S3/Cloud storage.
        String fileName = identifier + "-" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        String cloudUrl = "https://" + properties.getCloud().getBucketName() + ".s3.amazonaws.com/" + subDirectory + "/" + fileName;
        
        log.debug("Mock uploaded file to cloud: {}", cloudUrl);
        return cloudUrl;
    }

    @Override
    public void deleteFile(String fileUrl) {
        log.info("CloudStorageService: deleting file at {}", fileUrl);
        // Mock cloud delete logic.
    }

    @Override
    public Resource loadFileAsResource(String fileUrl) {
        log.info("CloudStorageService: loading resource from {}", fileUrl);
        // Mock cloud resource logic.
        return null;
    }
}
