package com.sbvia.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger UI / OpenAPI 3.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SBVIA API",
                version = "1.0",
                description = "API REST del Simulador de Comportamiento Vial con IA",
                contact = @Contact(
                        name = "Equipo SBVIA",
                        url = "https://github.com/keithdrox/SBVIA-AppWeb"
                )
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Token. Formato: Bearer [token]",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
