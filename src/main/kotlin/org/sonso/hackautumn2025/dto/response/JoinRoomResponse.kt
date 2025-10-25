package org.sonso.hackautumn2025.dto.response

import java.util.*

data class JoinRoomResponse(
    val roomId: UUID,
    val message: String
)