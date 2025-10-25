package org.sonso.hackautumn2025.dto.ws.chat.response

import org.sonso.hackautumn2025.dto.ws.chat.Message
import org.sonso.hackautumn2025.dto.ws.chat.enums.ResponseType
import java.util.*

data class InitResponse(
    val type: ResponseType = ResponseType.INIT,
    val uuid: UUID,
    val messages: List<Message>
)
