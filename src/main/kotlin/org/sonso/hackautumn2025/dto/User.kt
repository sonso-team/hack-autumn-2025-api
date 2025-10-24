package org.sonso.hackautumn2025.dto

import java.time.LocalDateTime

data class User(
    val id: String,
    val email: String,
    val nickname: String,
    val createdAt: LocalDateTime,
    val role: String,
    val avatarPath: String
)
