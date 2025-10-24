package org.sonso.hackautumn2025.dto.response

import org.sonso.hackautumn2025.dto.User

data class AuthenticationResponse(
    val message: String? = null,
    val token: String? = null,
    val user: User,
)
