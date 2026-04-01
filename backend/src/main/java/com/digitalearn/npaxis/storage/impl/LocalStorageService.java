package com.digitalearn.npaxis.storage.impl;

import com.digitalearn.npaxis.storage.StorageProperties;
import com.digitalearn.npaxis.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;

    @Override
    public String storeFile(MultipartFile file, String subDirectory, String identifier) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        validateFile(file, originalFilename);

        try {
            Path uploadPath = Paths.get(properties.getLocalDirectory(), subDirectory).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String fileExtension = getFileExtension(originalFilename);
            String safeBaseName = originalFilename.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = identifier + "-" + UUID.randomUUID() + "-" + safeBaseName;
            
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return properties.getLocalDirectory() + "/" + subDirectory + "/" + fileName;
        } catch (IOException ex) {
            log.error("Could not store file {}", originalFilename, ex);
            throw new RuntimeException("Could not store file " + originalFilename, ex);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            Path filePath = Paths.get(fileUrl).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Could not delete file at {}", fileUrl, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileUrl) {
        try {
            Path filePath = Paths.get(fileUrl).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + fileUrl);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not read file: " + fileUrl, ex);
        }
    }

    private void validateFile(MultipartFile file, String filename) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file " + filename);
        }
        if (filename.contains("..")) {
            throw new IllegalArgumentException("Cannot store file with relative path outside current directory " + filename);
        }
        String extension = getFileExtension(filename);
        if (!properties.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed: " + extension);
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        }
        return filename.substring(lastIndex + 1);
    }
}
