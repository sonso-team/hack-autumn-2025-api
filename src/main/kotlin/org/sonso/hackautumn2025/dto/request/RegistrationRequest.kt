package org.sonso.hackautumn2025.dto.request

data class RegistrationRequest(
    val nickname: String,
    val email: String,
    val password: String,
)
