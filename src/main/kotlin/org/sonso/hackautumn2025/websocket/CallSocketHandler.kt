package org.sonso.hackautumn2025.websocket

import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.dto.*
import org.sonso.hackautumn2025.repository.RoomRepository
import org.sonso.hackautumn2025.repository.UserRepository
import org.sonso.hackautumn2025.service.RoomSessionService
import org.springframework.context.event.EventListener
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import java.util.*

@Controller
class CallSocketHandler(
    private val messagingTemplate: SimpMessagingTemplate,
    private val roomSessionService: RoomSessionService,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository
) {

    private val logger = LoggerFactory.getLogger(CallSocketHandler::class.java)

    @MessageMapping("/join-room")
    fun joinRoom(
        @Payload message: JoinRoomMessage,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val sessionId = headerAccessor.sessionId ?: return
        val roomId = message.roomId

        // Определяем тип пользователя и получаем данные
        val (userId, nickname, avatarUrl, isGuest) = when {
            message.userId != null -> {
                val user = userRepository.findById(message.userId).orElse(null)

                if (user == null) {
                    logger.error("User ${message.userId} not found")
                    return
                }

                Tuple4(user.id, user.nickname, user.avatarPath, false)
            }
            message.guestName != null -> {
                Tuple4(null, message.guestName, null, true)
            }
            else -> {
                logger.error("No userId or guestName provided")
                return
            }
        }

        logger.info("${if (isGuest) "Guest" else "User"} $nickname joining room $roomId")

        // Сохраняем данные
        roomSessionService.joinRoom(roomId, sessionId, userId, nickname, avatarUrl, isGuest)

        val participantSessionIds = roomSessionService.getParticipants(roomId)

        // Собираем данные всех участников
        val participantsInfo = participantSessionIds.mapNotNull { sid ->
            val data = roomSessionService.getParticipantData(sid)
            data?.let {
                ParticipantInfo(sid, it.userId, it.nickname, it.avatarUrl, it.isGuest)
            }
        }

        logger.info("Participants: ${participantsInfo.map { it.nickname }}")

        // Отправляем список всем
        messagingTemplate.convertAndSend(
            "/topic/room/$roomId/participants",
            ParticipantsMessage(participantsInfo)
        )

        // Уведомляем остальных о новом участнике
        participantSessionIds
            .filter { it != sessionId }
            .forEach { participantId ->
                messagingTemplate.convertAndSend(
                    "/topic/room/$roomId/user-joined-$participantId",
                    UserJoinedMessage(sessionId, userId, nickname, avatarUrl, isGuest)
                )
            }
    }

    @MessageMapping("/offer")
    fun handleOffer(@Payload message: OfferMessage, headerAccessor: SimpMessageHeaderAccessor) {
        val sessionId = headerAccessor.sessionId ?: return
        messagingTemplate.convertAndSend(
            "/topic/room/offer/${message.target}",
            OfferFromMessage(message.offer, sessionId)
        )
    }

    @MessageMapping("/answer")
    fun handleAnswer(@Payload message: AnswerMessage, headerAccessor: SimpMessageHeaderAccessor) {
        val sessionId = headerAccessor.sessionId ?: return
        messagingTemplate.convertAndSend(
            "/topic/room/answer/${message.target}",
            AnswerFromMessage(message.answer, sessionId)
        )
    }

    @MessageMapping("/ice-candidate")
    fun handleIceCandidate(@Payload message: IceCandidateMessage, headerAccessor: SimpMessageHeaderAccessor) {
        val sessionId = headerAccessor.sessionId ?: return
        messagingTemplate.convertAndSend(
            "/topic/room/ice-candidate/${message.target}",
            IceCandidateFromMessage(message.candidate, sessionId)
        )
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        val sessionId = StompHeaderAccessor.wrap(event.message).sessionId ?: return
        val roomId = roomSessionService.leaveRoom(sessionId) ?: return

        messagingTemplate.convertAndSend(
            "/topic/room/$roomId/user-left",
            UserLeftMessage(sessionId)
        )

        val participantSessionIds = roomSessionService.getParticipants(roomId)
        val participantsInfo = participantSessionIds.mapNotNull { sid ->
            val data = roomSessionService.getParticipantData(sid)
            data?.let {
                ParticipantInfo(sid, it.userId, it.nickname, it.avatarUrl, it.isGuest)
            }
        }

        messagingTemplate.convertAndSend(
            "/topic/room/$roomId/participants",
            ParticipantsMessage(participantsInfo)
        )
    }

    fun closeConference(roomId: String, initiatorUserId: UUID) {
        val room = requireNotNull(roomRepository.findRoomEntityById(UUID.fromString(roomId))) {
            "Room is not exist"
        }

        if (room.owner.id != initiatorUserId) {
            throw AccessDeniedException("User not initiator and hasn't permissions to close this room")
        }


        val participantSessionIds = roomSessionService.getParticipants(roomId)
        participantSessionIds.forEach { sessionId ->
            messagingTemplate.convertAndSend(
                "/topic/room/$roomId/conference-ended",
                mapOf(
                    "roomId" to roomId,
                    "message" to "Конференция завершена администратором"
                )
            )
        }
        roomSessionService.removeRoom(roomId)
        logger.info("Conference $roomId forcibly ended by admin. Participants notified.")
    }
}

// Helper
private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
