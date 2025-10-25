package org.sonso.hackautumn2025.util

import org.sonso.hackautumn2025.dto.HistoryUnit
import org.sonso.hackautumn2025.dto.User
import org.sonso.hackautumn2025.dto.response.RoleResponse
import org.sonso.hackautumn2025.entity.RoleEntity
import org.sonso.hackautumn2025.entity.RoomEntity
import org.sonso.hackautumn2025.entity.UserEntity
import java.time.LocalDate

fun UserEntity.toUser() = User(
    id = id.toString(),
    nickname = nickname,
    email = email,
    createdAt = createdAt,
    role = role.name,
    avatarPath = avatarPath
)

fun RoleEntity.toResponseEventDto() = RoleResponse(
    id = this.id,
    name = this.name
)

fun RoomEntity.toHistoryUnit() = HistoryUnit(
    name = this.name,
    date = this.createdAt?.toLocalDate() ?: LocalDate.MIN,
    uuid = this.id
)
