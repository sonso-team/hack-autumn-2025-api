package org.sonso.hackautumn2025.dto.ws.chat.response

import org.sonso.hackautumn2025.dto.ws.chat.Message
import org.sonso.hackautumn2025.dto.ws.chat.enums.ResponseType

data class MessageResponse(
    val type: ResponseType = ResponseType.MESSAGE,
    val message: Message
)