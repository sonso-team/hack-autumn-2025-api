package org.sonso.hackautumn2025

import org.sonso.hackautumn2025.properties.AuthenticationProperties
import org.sonso.hackautumn2025.properties.MinioProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.socket.config.annotation.EnableWebSocket

@SpringBootApplication
@EnableWebSocket
@EnableAsync
@EnableConfigurationProperties(
    AuthenticationProperties::class,
    MinioProperties::class
)
class HackAutumn2025Application

fun main(args: Array<String>) {
    runApplication<HackAutumn2025Application>(*args)
}
