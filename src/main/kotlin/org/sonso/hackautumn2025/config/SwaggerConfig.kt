package org.sonso.hackautumn2025.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.security.SecuritySchemes
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8088", description = "ХАК")
    ],
    info = Info(
        title = "ОСЕНЬ ХАК 2025",
        description = "ОСЕНЬ ХАК 2025",
        version = "1.0.0",
        contact = Contact(
            name = "ТАКАЯ-ТО ТАКАЯ-ТО",
            url = "hack.kinoko.su"
        )
    ),
    security = [SecurityRequirement(name = "bearerAuth")]
)
@SecuritySchemes(
    SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT"
    )
)
class SwaggerConfig
