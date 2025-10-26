package org.sonso.hackautumn2025.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class ParticipantData(
    val userId: UUID? = null,
    val nickname: String,
    val avatarUrl: String? = null,
    val isGuest: Boolean = false
)

@Service
class RoomSessionService {

    private val logger = LoggerFactory.getLogger(RoomSessionService::class.java)

    private val rooms = ConcurrentHashMap<String, MutableList<String>>()
    private val sessionToRoom = ConcurrentHashMap<String, String>()
    private val sessionToParticipant = ConcurrentHashMap<String, ParticipantData>()

    fun joinRoom(
        roomId: String,
        sessionId: String,
        userId: UUID?,
        nickname: String,
        avatarUrl: String?,
        isGuest: Boolean
    ) {
        rooms.computeIfAbsent(roomId) { mutableListOf() }.add(sessionId)
        sessionToRoom[sessionId] = roomId
        sessionToParticipant[sessionId] = ParticipantData(userId, nickname, avatarUrl, isGuest)

        logger.info("${if (isGuest) "Guest" else "User"} $nickname joined room $roomId")
    }

    fun leaveRoom(sessionId: String): String? {
        val roomId = sessionToRoom.remove(sessionId) ?: return null
        val participant = sessionToParticipant.remove(sessionId)

        rooms[roomId]?.remove(sessionId)

        if (rooms[roomId]?.isEmpty() == true) {
            rooms.remove(roomId)
        }

        logger.info("${participant?.nickname} left room $roomId")
        return roomId
    }

    fun getParticipants(roomId: String): List<String> {
        return rooms[roomId]?.toList() ?: emptyList()
    }

    fun getParticipantData(sessionId: String): ParticipantData? {
        return sessionToParticipant[sessionId]
    }

    fun removeRoom(roomId: String) {
        rooms[roomId]?.forEach { sessionId -> sessionToParticipant.remove(sessionId) }
        rooms.remove(roomId)
    }
}
