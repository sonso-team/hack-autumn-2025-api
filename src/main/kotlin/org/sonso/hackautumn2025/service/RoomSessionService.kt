package org.sonso.hackautumn2025.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RoomSessionService {

    private val logger = LoggerFactory.getLogger(RoomSessionService::class.java)

    // roomId -> список sessionId участников
    private val rooms = ConcurrentHashMap<String, MutableList<String>>()

    // sessionId -> roomId (обратный индекс)
    private val sessionToRoom = ConcurrentHashMap<String, String>()

    /**
     * Добавить пользователя в комнату
     */
    fun joinRoom(roomId: String, sessionId: String) {
        rooms.computeIfAbsent(roomId) { mutableListOf() }.add(sessionId)
        sessionToRoom[sessionId] = roomId

        logger.info("Session $sessionId joined room $roomId")
    }

    /**
     * Удалить пользователя из комнаты
     */
    fun leaveRoom(sessionId: String): String? {
        val roomId = sessionToRoom.remove(sessionId) ?: return null

        rooms[roomId]?.remove(sessionId)

        // Если комната пуста - удаляем её
        if (rooms[roomId]?.isEmpty() == true) {
            rooms.remove(roomId)
            logger.info("Room $roomId deleted (empty)")
        }

        logger.info("Session $sessionId left room $roomId")
        return roomId
    }

    /**
     * Получить всех участников комнаты
     */
    fun getParticipants(roomId: String): List<String> {
        return rooms[roomId]?.toList() ?: emptyList()
    }

    /**
     * Получить комнату пользователя
     */
    fun getRoomId(sessionId: String): String? {
        return sessionToRoom[sessionId]
    }

    /**
     * Проверить, существует ли комната
     */
    fun roomExists(roomId: String): Boolean {
        return rooms.containsKey(roomId)
    }
}
