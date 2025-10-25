package org.sonso.hackautumn2025.dto.request

data class CreateRoomRequest(
    val name: String,
    val type: String,
    val description: String? = null,
    val accessCode: String? = null,
    val maxParticipants: Int? = null
)
