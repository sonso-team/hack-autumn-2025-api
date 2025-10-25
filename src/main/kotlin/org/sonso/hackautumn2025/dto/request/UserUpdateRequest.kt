package org.sonso.hackautumn2025.dto.request

data class UserUpdateRequest(
    val email: String?,
    val password: String?,
    val nickname: String?,
)
