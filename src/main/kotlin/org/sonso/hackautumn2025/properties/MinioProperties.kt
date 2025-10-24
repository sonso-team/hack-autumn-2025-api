package org.sonso.hackautumn2025.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.minio")
data class MinioProperties(
    val endpoint: String,
    val endpointDownload: String,
    val username: String,
    val password: String,
    val userAvatarsBucket: String,
)
