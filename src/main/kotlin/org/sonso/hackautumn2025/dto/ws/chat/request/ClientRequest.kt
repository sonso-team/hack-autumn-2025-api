package org.sonso.hackautumn2025.dto.ws.chat.request

import org.sonso.hackautumn2025.dto.ws.chat.enums.ClientAction

data class ClientRequest(
    val action: ClientAction,
    val text: String? = null
)