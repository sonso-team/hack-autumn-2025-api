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
        title = "ХАК DAUUUUUAY",
        description = "ОСЕНЬ ХАК 2025",
        version = "1.0.0",
        contact = Contact(
            name = "ТАКАЯ-ТО ТАКАЯ-ТО",
            email = "ivan.zapara.04@mail.ru",
            url = "https://yandex.ru/images/search?img_url=https%3A%2F%2Fi.pinimg.com" +
                "%2Foriginals%2F90%2F9b%2F82%2F909b82ec03a296c814bf6b544dd0b191.jpg" +
                "&lr=39&pos=0&rpt=simage&source=serp&text=смешные%20фото%20для%20группы%20с%20котами"
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
