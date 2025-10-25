package org.sonso.hackautumn2025.websocket

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.dto.ws.chat.Message
import org.sonso.hackautumn2025.dto.ws.chat.enums.ClientAction
import org.sonso.hackautumn2025.dto.ws.chat.enums.MessageType
import org.sonso.hackautumn2025.dto.ws.chat.request.ClientRequest
import org.sonso.hackautumn2025.dto.ws.chat.response.InitResponse
import org.sonso.hackautumn2025.dto.ws.chat.response.MessageResponse
import org.sonso.hackautumn2025.repository.RoomRepository
import org.sonso.hackautumn2025.repository.UserRepository
import org.sonso.hackautumn2025.util.ChatTechnicalMessagePatterns
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*
import java.util.concurrent.*

@Service
class ChatSocketHandler(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper()

    /** conferenceId -> sessions */
    private val sessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()

    /** sessionId -> conferenceId */
    private val sessionToConference = ConcurrentHashMap<String, String>()

    /** sessionId -> userId */
    private val sessionToUser = ConcurrentHashMap<String, UUID>()

    /** conferenceId -> (userId -> username) */
    private val conferenceUsers = ConcurrentHashMap<String, MutableMap<UUID, String>>()

    /** conferenceId -> messages history */
    private val messages = ConcurrentHashMap<String, MutableList<Message>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val parsed = parse(session) ?: run {
            session.close(CloseStatus.BAD_DATA.withReason("Некорректный путь"))
            return
        }

        val confKey = parsed.conferenceId.toString()
        val knownUsers = conferenceUsers.computeIfAbsent(confKey) { ConcurrentHashMap() }

        // 1) userId: из path (reconnect) или сгенерим
        val uid = parsed.uid ?: UUID.randomUUID()
        sessionToUser[session.id] = uid
        sessionToConference[session.id] = confKey

        // 2) username: из principal / query / известный по uid / иначе — Bad Data
        val username = resolveUsername(parsed, knownUsers[uid])
            ?: run {
                session.close(CloseStatus.BAD_DATA.withReason("Username обязателен для гостя"))
                cleanupSession(session)
                return
            }

        knownUsers.putIfAbsent(uid, username)

        // 3) зарегистрировать сессию
        sessions.computeIfAbsent(confKey) { CopyOnWriteArraySet() }.add(session)

        // 4) при первом пользователе — стартовая системка
        val confMessages = messages.computeIfAbsent(confKey) { mutableListOf() }
        if (confMessages.isEmpty()) {
            roomRepository.findRoomEntityById(UUID.fromString(confKey))
            appendMessage(
                confKey,
                Message(
                    MessageType.SYSTEM,
                    username,
                    ChatTechnicalMessagePatterns.CONF_START.format(username)
                )
            )
        } else {
            // при последующих — системка "подключился"
            val sys = Message(
                MessageType.SYSTEM,
                username,
                ChatTechnicalMessagePatterns.USER_CONNECT.format(username)
            )
            appendMessage(confKey, sys)
            broadcast(confKey, sys)
        }

        // 5) выдать INIT
        session.sendJSON(
            InitResponse(
                uuid = uid,
                messages = confMessages.toList()
            )
        )

        log.info("User $uid ($username) connected to $confKey")
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        if (message !is TextMessage) return
        val confKey = sessionToConference[session.id] ?: return
        val uid = sessionToUser[session.id] ?: return
        val username = conferenceUsers[confKey]?.get(uid) ?: "unknown"

        val evt = runCatching {
            mapper.readValue(message.payload, object : TypeReference<ClientRequest>() {})
        }.getOrElse {
            session.close(CloseStatus.BAD_DATA.withReason("Bad JSON: ${it.message}"))
            return
        }

        when (evt.action) {
            ClientAction.SEND -> {
                val text = (evt.text ?: "").trim()
                if (text.isEmpty()) return
                val msg = Message(MessageType.CHAT, username, text)
                appendMessage(confKey, msg)
                broadcast(confKey, msg)
            }

            ClientAction.END -> {
                // сообщаем всем и закрываем конфу
                val sys = Message(MessageType.SYSTEM, username, "Конференция завершена пользователем $username")
                appendMessage(confKey, sys)
                broadcast(confKey, sys)
                closeConference(
                    confKey,
                    uid,
                    CloseStatus.GOING_AWAY.withReason("Conference finished")
                )
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val confKey = sessionToConference[session.id] ?: return
        val uid = sessionToUser[session.id]
        val username = uid?.let { conferenceUsers[confKey]?.get(it) } ?: "unknown"

        sessions[confKey]?.remove(session)
        cleanupSession(session)

        // системка "отключился"
        val sys = Message(
            MessageType.SYSTEM,
            username,
            ChatTechnicalMessagePatterns.USER_DISCONNECT.format(username)
        )
        appendMessage(confKey, sys)
        broadcast(confKey, sys)

        // если сессий в конфе не осталось — можно почистить лёгкие структуры
        if (sessions[confKey].isNullOrEmpty()) {
            sessions.remove(confKey)
            log.info("Conference $confKey: no active sessions")
        }
    }

    private fun closeConference(
        confKey: String,
        initiator: UUID,
        status: CloseStatus
    ) {
        val room = requireNotNull(roomRepository.findRoomEntityById(UUID.fromString(confKey))) {
            "Room is not exist"
        }

        if (room.owner.id != initiator) {
            throw AccessDeniedException("User not initiator and hasn't permissions to close this room")
        }
        sessions[confKey]?.forEach { s ->
            runCatching { s.close(status) }
        }
        sessions.remove(confKey)
        conferenceUsers.remove(confKey)
        roomRepository.save(
            requireNotNull(roomRepository.findRoomEntityById(UUID.fromString(confKey))) {
                "Room is not exist"
            }.apply {
                this.history = mapper.writeValueAsString(messages[confKey])
            }
        )
        messages.remove(confKey)
        log.info("Conference $confKey disposed")
    }

    private fun cleanupSession(session: WebSocketSession) {
        sessionToUser.remove(session.id)
        sessionToConference.remove(session.id)
        sessions.remove(session.id)
    }

    private fun broadcast(confKey: String, msg: Message) {
        val payload = MessageResponse(message = msg)
        val json = mapper.writeValueAsString(payload)
        sessions[confKey]?.forEach { if (it.isOpen) it.sendMessage(TextMessage(json)) }
    }

    private fun appendMessage(confKey: String, msg: Message) {
        val list = messages.computeIfAbsent(confKey) { mutableListOf() }
        list += msg
    }

    private fun WebSocketSession.sendJSON(obj: Any) =
        sendMessage(TextMessage(mapper.writeValueAsString(obj)))

    // ===== URL parsing & username resolving =====
    private data class Parsed(
        val conferenceId: UUID,
        val uid: UUID?,
        val username: String?
    )

    private fun parse(session: WebSocketSession): Parsed? {
        val uri = session.uri ?: return null
        val params = uri.rawQuery
            ?.split("&")
            ?.mapNotNull {
                val i = it.indexOf('=')
                if (i <= 0) null else
                    it.substring(0, i) to java.net.URLDecoder.decode(it.substring(i + 1), Charsets.UTF_8)
            }
            ?.toMap().orEmpty()

        val conf = params["conf"]?.let { runCatching { UUID.fromString(it) }.getOrNull() } ?: return null
        val uid = params["uid"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        val username = params["username"]

        return Parsed(conf, uid, username)
    }

    private fun resolveUsername(
        parsed: Parsed,
        knownByUid: String?
    ): String? {
        // 1) если уже знаем имя uida — ок
        if (!knownByUid.isNullOrBlank()) return knownByUid

        // 2) авторизован?
        if (parsed.uid != null) {
            val principalName = userRepository.findUserEntityById(parsed.uid)?.username
            if (principalName != null) return principalName
        }

        // 3) гость обязан прислать username (query)
        val uname = parsed.username?.takeIf { it.isNotBlank() }
        return uname
    }
}
