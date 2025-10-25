package org.sonso.hackautumn2025.dto.ws.chat

import org.sonso.hackautumn2025.dto.ws.chat.enums.MessageType

data class Message(
    val type: MessageType,
    val username: String? = null,
    val text: String
)