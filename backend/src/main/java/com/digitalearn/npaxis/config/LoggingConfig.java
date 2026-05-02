package com.digitalearn.npaxis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration class to ensure logging directory exists on application startup.
 * This prevents logs from being written to temporary directories and ensures
 * persistent log file storage.
 *
 * @author NPAxis Team
 */
@Configuration
@Slf4j
public class LoggingConfig {

    /**
     * Initialize logging directory on application startup.
     * This method ensures that the logs directory is created if it doesn't exist,
     * allowing log files to be persisted to disk.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeLoggingDirectory() {
        try {
            // Get the log directory path from environment or use default
            String logDirPath = System.getProperty("LOG_FILE_PATH", "./logs");

            // Create Path object
            Path logDir = Paths.get(logDirPath);

            // Create directory if it doesn't exist
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                log.info("Created logging directory: {}", logDir.toAbsolutePath());
            } else {
                log.info("Logging directory exists: {}", logDir.toAbsolutePath());
            }

            // Create archive subdirectory
            Path archiveDir = logDir.resolve("archive");
            if (!Files.exists(archiveDir)) {
                Files.createDirectories(archiveDir);
                log.info("Created archive directory: {}", archiveDir.toAbsolutePath());
            }

            // Verify write permissions
            File logDirFile = logDir.toFile();
            if (!logDirFile.canWrite()) {
                log.warn("Warning: Logging directory is not writable! Check file permissions.");
            }

            log.info("Logging configuration initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize logging directory", e);
            // Don't throw exception as this shouldn't prevent application startup
        }
    }
}

