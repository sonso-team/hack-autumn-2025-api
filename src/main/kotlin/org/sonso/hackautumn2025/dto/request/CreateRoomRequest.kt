package org.sonso.hackautumn2025.dto.request

import org.sonso.hackautumn2025.properties.RoomType

data class CreateRoomRequest(
    val name: String,
    val type: String,
    val description: String? = null,
    val accessCode: String? = null,
    val maxParticipants: Int? = null
)
