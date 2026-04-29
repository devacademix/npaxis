package com.digitalearn.npaxis.pdf.service.impl;

import com.digitalearn.npaxis.pdf.service.ByteStorageService;
import com.digitalearn.npaxis.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Local file system implementation of ByteStorageService.
 * Stores generated PDFs to the local file system.
 * Active only in 'dev' profile.
 */
@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class LocalByteStorageService implements ByteStorageService {

    private final StorageProperties properties;

    @Override
    public String storeBytes(byte[] content, String subDirectory, String fileName, String contentType) {
        validateInput(content, subDirectory, fileName);

        try {
            // Create subdirectory path
            Path uploadPath = Paths.get(properties.getLocalDirectory(), subDirectory)
                    .toAbsolutePath()
                    .normalize();

            // Ensure directory exists
            Files.createDirectories(uploadPath);

            // Resolve target file path
            Path targetLocation = uploadPath.resolve(fileName).normalize();

            // Prevent directory traversal attacks
            if (!targetLocation.getParent().equals(uploadPath)) {
                throw new IllegalArgumentException(
                        "Invalid file path: attempted directory traversal"
                );
            }

            // Write bytes to file
            Files.write(targetLocation,
                    content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            String filePath = properties.getLocalDirectory() + "/" + subDirectory + "/" + fileName;
            log.info("PDF stored locally: {} (size: {} bytes)", filePath, content.length);

            return filePath;

        } catch (IOException ex) {
            log.error("Failed to store PDF bytes to file system: {}", fileName, ex);
            throw new RuntimeException("Failed to store PDF file: " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
            log.info("PDF deleted: {}", filePath);
        } catch (IOException ex) {
            log.error("Failed to delete PDF: {}", filePath, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + filePath);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load PDF resource: " + filePath, ex);
        }
    }

    /**
     * Validates input parameters.
     */
    private void validateInput(byte[] content, String subDirectory, String fileName) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        if (!StringUtils.hasText(subDirectory)) {
            throw new IllegalArgumentException("Subdirectory cannot be empty");
        }

        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name: path separators not allowed");
        }
    }
}

