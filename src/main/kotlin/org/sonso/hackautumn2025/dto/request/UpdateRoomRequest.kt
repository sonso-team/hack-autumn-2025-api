package org.sonso.hackautumn2025.dto.request

// Request для обновления комнаты
data class UpdateRoomRequest(
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
    val accessCode: String? = null,
    val maxParticipants: Int? = null
)
