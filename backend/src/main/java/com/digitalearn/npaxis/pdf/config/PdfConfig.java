package com.digitalearn.npaxis.pdf.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for PDF generation module.
 * <p>
 * Configures beans for PDF template processing.
 * Uses the globally shared TemplateEngine from Spring Boot autoconfiguration.
 * This ensures consistent behavior with web templates.
 */
@Slf4j
@Configuration
public class PdfConfig {

    /**
     * The primary TemplateEngine bean is auto-configured by Spring Boot.
     * We rely on Spring Boot's default configuration which:
     * - Resolves templates from "classpath:/templates/"
     * - Uses TemplateMode.HTML
     * - Caches templates in production
     *
     * No additional configuration is needed - the auto-configured
     * TemplateEngine is used by TemplateRenderer via dependency injection.
     */

    /**
     * Post-initialization logging to confirm PDF module is ready.
     */
    @Bean
    public PdfModuleInitializer pdfModuleInitializer() {
        return new PdfModuleInitializer();
    }

    /**
     * Simple initializer bean for logging purposes.
     */
    @Slf4j
    public static class PdfModuleInitializer {
        public PdfModuleInitializer() {
            log.info("✓ PDF Generation Module initialized");
            log.info("  - Templates: classpath:/templates/pdf/");
            log.info("  - Engine: Thymeleaf (HTML mode)");
            log.info("  - Converter: Flying Saucer (XHTMLRenderer)");
            log.info("  - Storage: ByteStorageService");
        }
    }
}


