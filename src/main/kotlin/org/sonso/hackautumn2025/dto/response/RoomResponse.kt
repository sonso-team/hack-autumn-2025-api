package org.sonso.hackautumn2025.dto.response

import java.time.LocalDateTime
import java.util.*

// Response с данными комнаты
data class RoomResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val ownerId: UUID,
    val ownerNickname: String,
    val status: String?,
    val type: String,
    val accessCode: String?,
    val maxParticipants: Int?,
    val participantCount: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
