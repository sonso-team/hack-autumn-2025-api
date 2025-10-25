package org.sonso.hackautumn2025.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

// Сообщение о присоединении к комнате
data class JoinRoomMessage(
    @JsonProperty("roomId")
    val roomId: String,

    @JsonProperty("userId")
    val userId: UUID? = null,

    @JsonProperty("guestName")
    val guestName: String? = null
)

// WebRTC Offer
data class OfferMessage(
    @JsonProperty("offer")
    val offer: Map<String, Any>,
    @JsonProperty("target")
    val target: String
)

// WebRTC Answer
data class AnswerMessage(
    @JsonProperty("answer")
    val answer: Map<String, Any>,
    @JsonProperty("target")
    val target: String
)

// ICE Candidate
data class IceCandidateMessage(
    @JsonProperty("candidate")
    val candidate: Map<String, Any>,
    @JsonProperty("target")
    val target: String
)

data class ParticipantInfo(
    @JsonProperty("sessionId")
    val sessionId: String,

    @JsonProperty("userId")
    val userId: UUID? = null,

    @JsonProperty("nickname")
    val nickname: String,

    @JsonProperty("avatarUrl")
    val avatarUrl: String? = null,

    @JsonProperty("isGuest")
    val isGuest: Boolean = false
)

// Исходящие сообщения
data class ParticipantsMessage(
    @JsonProperty("participants")
    val participants: List<ParticipantInfo>
)

data class UserJoinedMessage(
    @JsonProperty("sessionId")
    val sessionId: String,

    @JsonProperty("userId")
    val userId: UUID? = null,

    @JsonProperty("nickname")
    val nickname: String,

    @JsonProperty("avatarUrl")
    val avatarUrl: String? = null,

    @JsonProperty("isGuest")
    val isGuest: Boolean = false
)

data class UserLeftMessage(
    @JsonProperty("socketId")
    val socketId: String
)

data class OfferFromMessage(
    @JsonProperty("offer")
    val offer: Map<String, Any>,
    @JsonProperty("from")
    val from: String
)

data class AnswerFromMessage(
    @JsonProperty("answer")
    val answer: Map<String, Any>,
    @JsonProperty("from")
    val from: String
)

data class IceCandidateFromMessage(
    @JsonProperty("candidate")
    val candidate: Map<String, Any>,
    @JsonProperty("from")
    val from: String
)
