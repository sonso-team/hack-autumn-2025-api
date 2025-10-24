package org.sonso.hackautumn2025.config

import io.minio.MinioClient
import org.sonso.hackautumn2025.properties.MinioProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig(
    private val minioProperties: MinioProperties
) {
    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(minioProperties.endpoint)
            .credentials(minioProperties.username, minioProperties.password)
            .build()
    }
}
