package org.sonso.hackautumn2025.config.websocket

import org.sonso.hackautumn2025.websocket.CallSocketHandler
import org.springframework.stereotype.Component
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Component
class CallSocketServer(
    private val handler: CallSocketHandler,
): WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(handler, "/ws/call/**")
            .setAllowedOriginPatterns("*")
    }
}
