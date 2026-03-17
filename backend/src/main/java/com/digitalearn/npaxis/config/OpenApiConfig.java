package com.digitalearn.npaxis.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * OpenApiConfig sets up the OpenAPI documentation metadata and JWT bearer security schema.
 * This configuration enables Swagger UI to use JWT tokens for secured endpoints.
 * This is the configuration for the NPaxis APIs.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "NPaxis",
                description = "NPaxis is a platform which allows efficient Student-Preceptor communication.",
                version = "v1.0.0"
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Enter your JWT Auth Token here...",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // No implementation needed; this class purely holds OpenAPI metadata annotations
}
