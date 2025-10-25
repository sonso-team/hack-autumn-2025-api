package org.sonso.hackautumn2025.service

import org.sonso.hackautumn2025.dto.*
import org.sonso.hackautumn2025.dto.request.CreateRoomRequest
import org.sonso.hackautumn2025.dto.request.UpdateRoomRequest
import org.sonso.hackautumn2025.dto.response.JoinRoomResponse
import org.sonso.hackautumn2025.dto.response.RoomResponse
import org.sonso.hackautumn2025.entity.RoomEntity
import org.sonso.hackautumn2025.entity.RoomParticipantEntity
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.repository.RoomParticipantRepository
import org.sonso.hackautumn2025.repository.RoomRepository
import org.sonso.hackautumn2025.util.toRoomResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val roomParticipantRepository: RoomParticipantRepository
) {

    @Transactional
    fun createRoom(request: CreateRoomRequest, owner: UserEntity): UUID {
        val room = RoomEntity().apply {
            name = request.name
            description = request.description
            this.owner = owner
            type = request.type
            accessCode = request.accessCode
            maxParticipants = request.maxParticipants
            status = "ACTIVE"
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        val savedRoom = roomRepository.save(room)

        // Автоматически добавляем создателя как участника
        val participant = RoomParticipantEntity().apply {
            this.room = savedRoom
            userId = owner.id
            joinedAt = LocalDateTime.now()
        }
        roomParticipantRepository.save(participant)

        return savedRoom.id
    }

    @Transactional(readOnly = true)
    fun getRoomById(roomId: UUID): RoomResponse {
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Room not found with id: $roomId") }

        return toRoomResponse(room)
    }

    @Transactional(readOnly = true)
    fun getAllRooms(): List<RoomResponse> {
        return roomRepository.findAll().map { toRoomResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getActiveRooms(): List<RoomResponse> {
        return roomRepository.findByStatus("ACTIVE").map { toRoomResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getUserRooms(userId: UUID): List<RoomResponse> {
        return roomRepository.findByOwnerId(userId).map { toRoomResponse(it) }
    }

    @Transactional
    fun updateRoom(roomId: UUID, request: UpdateRoomRequest, userId: UUID): RoomResponse {
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Room not found with id: $roomId") }

        // Проверка прав доступа
        if (room.owner.id != userId) {
            throw IllegalArgumentException("You are not the owner of this room")
        }

        request.name?.let { room.name = it }
        request.description?.let { room.description = it }
        request.status?.let { room.status = it }
        request.accessCode?.let { room.accessCode = it }
        request.maxParticipants?.let { room.maxParticipants = it }
        room.updatedAt = LocalDateTime.now()

        val updatedRoom = roomRepository.save(room)
        return toRoomResponse(updatedRoom)
    }

    @Transactional
    fun deleteRoom(roomId: UUID, userId: UUID) {
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Room not found with id: $roomId") }

        if (room.owner.id != userId) {
            throw IllegalArgumentException("You are not the owner of this room")
        }

        roomRepository.delete(room)
    }

    @Transactional
    fun joinRoom(roomId: UUID, userId: UUID, accessCode: String?): JoinRoomResponse {
        val room = roomRepository.findByIdAndStatus(roomId, "ACTIVE")
            .orElseThrow { IllegalArgumentException("Room not found or inactive") }

        // Проверка пароля для приватных комнат
        if (room.type == "PASSWORD_PROTECTED") {
            if (room.accessCode != accessCode) {
                throw IllegalArgumentException("Invalid access code")
            }
        }

        // Проверка лимита участников
        val currentParticipants = roomParticipantRepository.findByRoomId(roomId)
            .filter { it.leftAt == null }
            .size

        if (room.maxParticipants != null && currentParticipants >= room.maxParticipants!!) {
            throw IllegalArgumentException("Room is full")
        }

        // Проверяем, есть ли уже участник
        val existingParticipant = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)

        if (existingParticipant.isPresent) {
            val participant = existingParticipant.get()
            if (participant.leftAt != null) {
                // Повторное присоединение
                participant.joinedAt = LocalDateTime.now()
                participant.leftAt = null
                roomParticipantRepository.save(participant)
            }
        } else {
            // Новый участник
            val participant = RoomParticipantEntity().apply {
                this.room = room
                this.userId = userId
                joinedAt = LocalDateTime.now()
            }
            roomParticipantRepository.save(participant)
        }

        return JoinRoomResponse(
            roomId = room.id,
            message = "Successfully joined room: ${room.name}"
        )
    }

    @Transactional
    fun leaveRoom(roomId: UUID, userId: UUID) {
        val participant = roomParticipantRepository.findByRoomIdAndUserId(roomId, userId)
            .orElseThrow { IllegalArgumentException("You are not a participant of this room") }

        participant.leftAt = LocalDateTime.now()
        roomParticipantRepository.save(participant)
    }

    @Transactional(readOnly = true)
    fun getRoomParticipants(roomId: UUID): List<UUID> {
        return roomParticipantRepository.findByRoomId(roomId)
            .filter { it.leftAt == null }
            .map { it.userId }
    }

    private fun toRoomResponse(room: RoomEntity): RoomResponse {
        val participantCount = roomParticipantRepository.findByRoomId(room.id)
            .filter { it.leftAt == null }
            .size

        return room.toRoomResponse(participantCount)
    }
}
