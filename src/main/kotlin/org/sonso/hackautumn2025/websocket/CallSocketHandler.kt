package org.sonso.hackautumn2025.websocket

import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.dto.*
import org.sonso.hackautumn2025.service.RoomSessionService
import org.springframework.context.event.EventListener
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Controller
class CallSocketHandler(
    private val messagingTemplate: SimpMessagingTemplate,
    private val roomSessionService: RoomSessionService,
) {

    private val logger = LoggerFactory.getLogger(CallSocketHandler::class.java)

    @MessageMapping("/join-room")
    fun joinRoom(
        @Payload message: JoinRoomMessage,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val sessionId = headerAccessor.sessionId ?: return
        val roomId = message.roomId

        logger.info("Session $sessionId attempting to join room $roomId")

        // Добавляем пользователя в комнату
        roomSessionService.joinRoom(roomId, sessionId)

        // Получаем всех участников
        val participants = roomSessionService.getParticipants(roomId)

        logger.info("👥 Current participants in room $roomId: $participants")

        // Отправляем всем участникам обновлённый список
        messagingTemplate.convertAndSend(
            "/topic/room/$roomId/participants",
            ParticipantsMessage(participants)
        )

        // Уведомляем других участников (НЕ себя) о новом пользователе
        // Используем broadcast в topic вместо convertAndSendToUser
        participants
            .filter { it != sessionId }
            .forEach { participantId ->
                logger.info("Notifying $participantId about new user $sessionId")

                // Отправляем через topic, но с фильтром по destination
                messagingTemplate.convertAndSend(
                    "/topic/room/$roomId/user-joined-$participantId",
                    UserJoinedMessage(sessionId)
                )
            }

        logger.info("$sessionId connected to room $roomId with ${participants.size} total participants")
    }

    @MessageMapping("/offer")
    fun handleOffer(
        @Payload message: OfferMessage,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val sessionId = headerAccessor.sessionId ?: return

        logger.info("Offer from $sessionId to ${message.target}")

        // Используем broadcast через topic с target в пути
        messagingTemplate.convertAndSend(
            "/topic/room/offer/${message.target}",
            OfferFromMessage(message.offer, sessionId)
        )
    }

    @MessageMapping("/answer")
    fun handleAnswer(
        @Payload message: AnswerMessage,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val sessionId = headerAccessor.sessionId ?: return

        logger.info("Answer from $sessionId to ${message.target}")

        // Используем broadcast через topic
        messagingTemplate.convertAndSend(
            "/topic/room/answer/${message.target}",
            AnswerFromMessage(message.answer, sessionId)
        )
    }

    @MessageMapping("/ice-candidate")
    fun handleIceCandidate(
        @Payload message: IceCandidateMessage,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val sessionId = headerAccessor.sessionId ?: return

        logger.info("ICE candidate from $sessionId to ${message.target}")

        // Используем broadcast через topic
        messagingTemplate.convertAndSend(
            "/topic/room/ice-candidate/${message.target}",
            IceCandidateFromMessage(message.candidate, sessionId)
        )
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        val sessionId = StompHeaderAccessor.wrap(event.message).sessionId ?: return

        logger.info("Session $sessionId disconnecting")

        // Удаляем пользователя из комнаты
        val roomId = roomSessionService.leaveRoom(sessionId) ?: return

        // Уведомляем остальных участников
        messagingTemplate.convertAndSend(
            "/topic/room/$roomId/user-left",
            UserLeftMessage(sessionId)
        )

        // Отправляем обновлённый список участников
        val participants = roomSessionService.getParticipants(roomId)
        messagingTemplate.convertAndSend(
            "/topic/room/$roomId/participants",
            ParticipantsMessage(participants)
        )

        logger.info("$sessionId disconnected from room $roomId. Remaining: ${participants.size}")
    }
}
