package com.digitalearn.npaxis.pdf.service;

import com.digitalearn.npaxis.pdf.exception.PdfGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Renders Thymeleaf templates to HTML strings.
 * Handles template resolution, data injection, and error handling.
 * Thread-safe and stateless - safe for concurrent usage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateRenderer {

    private final TemplateEngine templateEngine;

    /**
     * Renders a Thymeleaf template with provided data to HTML string.
     * Templates are expected to be in templates/pdf/ directory.
     *
     * @param templateName the template name (e.g., "invoice", "certificate")
     * @param data         the data model to inject into template
     * @return rendered HTML as string
     * @throws PdfGenerationException if template not found or rendering fails
     */
    public String renderTemplate(String templateName, Map<String, Object> data) {
        try {
            PdfUtils.validateTemplateName(templateName);

            log.debug("Rendering Thymeleaf template: {}", templateName);

            // Create Spring context with N-fold safe data
            Context context = new Context();
            if (data != null) {
                context.setVariables(data);
            }

            // Full template path: "pdf/templateName"
            String templatePath = "pdf/" + templateName;
            String html = templateEngine.process(templatePath, context);

            log.debug("Template rendered successfully: {} (size: {} chars)", templateName, html.length());
            return html;

        } catch (Exception e) {
            String errorMsg = String.format(
                    "Failed to render Thymeleaf template '%s': %s",
                    templateName,
                    e.getMessage()
            );
            log.error(errorMsg, e);

            throw new PdfGenerationException(
                    errorMsg,
                    templateName,
                    "Template rendering failed",
                    e
            );
        }
    }

    /**
     * Renders a template with null-safe data handling.
     * If data is null, an empty context is used.
     *
     * @param templateName the template name
     * @return rendered HTML
     * @throws PdfGenerationException if renderings fails
     */
    public String renderTemplate(String templateName) {
        return renderTemplate(templateName, null);
    }
}

