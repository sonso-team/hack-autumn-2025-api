package org.sonso.hackautumn2025.util

import org.sonso.hackautumn2025.dto.User
import org.sonso.hackautumn2025.dto.response.RoleResponse
import org.sonso.hackautumn2025.dto.response.RoomResponse
import org.sonso.hackautumn2025.entity.RoleEntity
import org.sonso.hackautumn2025.entity.RoomEntity
import org.sonso.hackautumn2025.entity.UserEntity

fun UserEntity.toUser() = User(
    id = id.toString(),
    nickname = nickname,
    email = email,
    createdAt = createdAt,
    role = role.name,
    avatarPath = avatarPath
)

fun RoleEntity.convertToResponseEventDto() = RoleResponse(
    id = this.id,
    name = this.name
)

fun RoomEntity.toRoomResponse(participantCount: Int) = RoomResponse(
    id = id,
    name = name,
    description = description,
    ownerId = owner.id,
    ownerNickname = owner.nickname,
    status = status,
    type = type,
    accessCode = accessCode,
    maxParticipants = maxParticipants,
    participantCount = participantCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)
