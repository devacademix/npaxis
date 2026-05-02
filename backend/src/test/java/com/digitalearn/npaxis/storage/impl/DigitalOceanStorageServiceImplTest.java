package com.digitalearn.npaxis.storage.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.digitalearn.npaxis.exceptions.*;
import com.digitalearn.npaxis.storage.config.DigitalOceanStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
//import java.time.Duration;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for DigitalOceanStorageServiceImpl.
// * Tests cover all core functionality including file storage, deletion, retrieval, and error handling.
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("DigitalOcean Storage Service Implementation Tests")
//class DigitalOceanStorageServiceImplTest {
//
//    @Mock
//    private AmazonS3 amazonS3;
//
//    @Mock
//    private TransferManager transferManager;
//
//    @Mock
//    private Upload upload;
//
//    @Mock
//    private MultipartFile mockFile;
//
//    @Mock
//    private S3Object s3Object;
//
//    @InjectMocks
//    private DigitalOceanStorageServiceImpl storageService;
//
//    private DigitalOceanStorageProperties properties;
//
//    @BeforeEach
//    void setUp() {
//        properties = new DigitalOceanStorageProperties();
//        properties.setEndpoint("https://nyc3.digitaloceanspaces.com");
//        properties.setRegion("nyc3");
//        properties.setAccessKey("test-key");
//        properties.setSecretKey("test-secret");
//        properties.setBucketName("test-bucket");
//        properties.setCdnEndpoint("https://cdn.example.com");
//        properties.setMaxFileSizeBytes(10485760L);
//        properties.setPresignedUrlExpiry(Duration.ofHours(1));
//
//        storageService = new DigitalOceanStorageServiceImpl(amazonS3, transferManager, properties);
//    }
//
//    // ==================== storeFile Tests ====================
//
//    @Test
//    @DisplayName("Should successfully store file and return CDN URL")
//    void testStoreFileSuccess() throws Exception {
//        // Arrange
//        String content = "test file content";
//        byte[] fileContent = content.getBytes();
//
//        when(mockFile.isEmpty()).thenReturn(false);
//        when(mockFile.getSize()).thenReturn((long) fileContent.length);
//        when(mockFile.getContentType()).thenReturn("image/jpeg");
//        when(mockFile.getOriginalFilename()).thenReturn("avatar.jpg");
//        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
//
//        when(transferManager.upload(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
//                .thenReturn(upload);
//
//        // Act
//        String result = storageService.storeFile(mockFile, "profiles", "user123");
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.startsWith("https://cdn.example.com/"));
//        verify(transferManager, times(1)).upload(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
//        verify(amazonS3, times(1)).setObjectAcl(anyString(), anyString(), any(CannedAccessControlList.class));
//    }
//
//    @Test
//    @DisplayName("Should throw StorageException when file is null")
//    void testStoreFileNullFile() {
//        // Act & Assert
//        assertThrows(StorageException.class, () ->
//            storageService.storeFile(null, "profiles", "user123")
//        );
//    }
//
//    @Test
//    @DisplayName("Should throw StorageException when file is empty")
//    void testStoreFileEmptyFile() {
//        // Arrange
//        when(mockFile.isEmpty()).thenReturn(true);
//
//        // Act & Assert
//        assertThrows(StorageException.class, () ->
//            storageService.storeFile(mockFile, "profiles", "user123")
//        );
//    }
//
//    @Test
//    @DisplayName("Should throw StorageFileSizeExceededException when file exceeds max size")
//    void testStoreFileExceedsMaxSize() {
//        // Arrange
//        when(mockFile.isEmpty()).thenReturn(false);
//        when(mockFile.getSize()).thenReturn(20971520L); // 20 MB, exceeds 10 MB limit
//
//        // Act & Assert
//        StorageFileSizeExceededException exception = assertThrows(
//            StorageFileSizeExceededException.class,
//            () -> storageService.storeFile(mockFile, "profiles", "user123")
//        );
//
//        assertEquals(20971520L, exception.getFileSize());
//        assertEquals(10485760L, exception.getMaxAllowed());
//    }
//
//    @Test
//    @DisplayName("Should throw StorageUnsupportedFileTypeException for disallowed MIME type")
//    void testStoreFileUnsupportedMimeType() {
//        // Arrange
//        when(mockFile.isEmpty()).thenReturn(false);
//        when(mockFile.getSize()).thenReturn(1024L);
//        when(mockFile.getContentType()).thenReturn("application/exe");
//
//        // Act & Assert
//        StorageUnsupportedFileTypeException exception = assertThrows(
//            StorageUnsupportedFileTypeException.class,
//            () -> storageService.storeFile(mockFile, "profiles", "user123")
//        );
//
//        assertEquals("application/exe", exception.getContentType());
//    }
//
//    // ==================== deleteFile Tests ====================
//
//    @Test
//    @DisplayName("Should successfully delete file")
//    void testDeleteFileSuccess() {
//        // Arrange
//        String fileUrl = "https://cdn.example.com/profiles/user123_uuid_avatar.jpg";
//
//        // Act
//        storageService.deleteFile(fileUrl);
//
//        // Assert
//        verify(amazonS3, times(1)).deleteObject(properties.getBucketName(), "profiles/user123_uuid_avatar.jpg");
//    }
//
//    @Test
//    @DisplayName("Should silently handle 404 error (idempotent delete)")
//    void testDeleteFileNotFound() {
//        // Arrange
//        String fileUrl = "https://cdn.example.com/profiles/nonexistent.jpg";
//
//        AmazonServiceException s3Exception = new AmazonServiceException("Not Found");
//        s3Exception.setStatusCode(404);
//
//        doThrow(s3Exception).when(amazonS3).deleteObject(anyString(), anyString());
//
//        // Act - should not throw
//        storageService.deleteFile(fileUrl);
//
//        // Assert
//        verify(amazonS3, times(1)).deleteObject(anyString(), anyString());
//    }
//
//    @Test
//    @DisplayName("Should throw StorageException when delete fails with non-404 error")
//    void testDeleteFileWithError() {
//        // Arrange
//        String fileUrl = "https://cdn.example.com/profiles/avatar.jpg";
//
//        AmazonServiceException s3Exception = new AmazonServiceException("Access Denied");
//        s3Exception.setStatusCode(403);
//
//        doThrow(s3Exception).when(amazonS3).deleteObject(anyString(), anyString());
//
//        // Act & Assert
//        assertThrows(StorageException.class, () ->
//            storageService.deleteFile(fileUrl)
//        );
//    }
//
//    @Test
//    @DisplayName("Should throw StorageException for null file URL")
//    void testDeleteFileNullUrl() {
//        // Act & Assert
//        assertThrows(StorageException.class, () ->
//            storageService.deleteFile(null)
//        );
//    }
//
//    // ==================== loadFileAsResource Tests ====================
//
//    @Test
//    @DisplayName("Should successfully load file as resource")
//    void testLoadFileAsResourceSuccess() {
//        // Arrange
//        String fileUrl = "https://cdn.example.com/profiles/user123_uuid_avatar.jpg";
//        String objectKey = "profiles/user123_uuid_avatar.jpg";
//
//        when(amazonS3.doesObjectExist(properties.getBucketName(), objectKey)).thenReturn(true);
//        when(amazonS3.getObject(properties.getBucketName(), objectKey)).thenReturn(s3Object);
//
//        // Act
//        Resource resource = storageService.loadFileAsResource(fileUrl);
//
//        // Assert
//        assertNotNull(resource);
//        verify(amazonS3, times(1)).doesObjectExist(properties.getBucketName(), objectKey);
//        verify(amazonS3, times(1)).getObject(properties.getBucketName(), objectKey);
//    }
//
//    @Test
//    @DisplayName("Should throw StorageFileNotFoundException when file not found")
//    void testLoadFileAsResourceNotFound() {
//        // Arrange
//        String fileUrl = "https://cdn.example.com/profiles/nonexistent.jpg";
//        String objectKey = "profiles/nonexistent.jpg";
//
//        when(amazonS3.doesObjectExist(properties.getBucketName(), objectKey)).thenReturn(false);
//
//        // Act & Assert
//        assertThrows(StorageFileNotFoundException.class, () ->
//            storageService.loadFileAsResource(fileUrl)
//        );
//    }
//
//    @Test
//    @DisplayName("Should throw StorageFileNotFoundException for null file URL")
//    void testLoadFileAsResourceNullUrl() {
//        // Act & Assert
//        assertThrows(StorageFileNotFoundException.class, () ->
//            storageService.loadFileAsResource(null)
//        );
//    }
//
//    // ==================== presignedUrl Tests ====================
//
//    @Test
//    @DisplayName("Should successfully generate presigned URL")
//    void testGeneratePresignedUrlSuccess() {
//        // Arrange
//        String objectKey = "profiles/user123_uuid_avatar.jpg";
//        try {
//            java.net.URL mockUrl = new java.net.URL("https://cdn.example.com/profiles/user123_uuid_avatar.jpg?X-Amz-Algorithm=...");
//            when(amazonS3.generatePresignedUrl(any())).thenReturn(mockUrl);
//
//            // Act
//            String presignedUrl = storageService.generatePresignedUrl(objectKey, Duration.ofMinutes(30));
//
//            // Assert
//            assertNotNull(presignedUrl);
//            assertTrue(presignedUrl.contains("X-Amz-Algorithm"));
//            verify(amazonS3, times(1)).generatePresignedUrl(any());
//        } catch (Exception _) {
//            fail("Failed to create mock URL");
//        }
//    }
//
//    @Test
//    @DisplayName("Should throw StorageException for null object key in presigned URL generation")
//    void testGeneratePresignedUrlNullKey() {
//        // Act & Assert
//        assertThrows(StorageException.class, () ->
//            storageService.generatePresignedUrl(null, Duration.ofHours(1))
//        );
//    }
//
//    @Test
//    @DisplayName("Should use default expiry when none provided")
//    void testGeneratePresignedUrlDefaultExpiry() {
//        // Arrange
//        String objectKey = "profiles/avatar.jpg";
//        try {
//            java.net.URL mockUrl = new java.net.URL("https://example.com/file");
//            when(amazonS3.generatePresignedUrl(any())).thenReturn(mockUrl);
//
//            // Act
//            String presignedUrl = storageService.generatePresignedUrl(objectKey, null);
//
//            // Assert
//            assertNotNull(presignedUrl);
//            verify(amazonS3, times(1)).generatePresignedUrl(any());
//        } catch (Exception _) {
//            fail("Failed to create mock URL");
//        }
//    }
//}
//
//
//
//
//
//
//
