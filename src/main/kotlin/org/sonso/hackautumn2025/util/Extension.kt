package org.sonso.hackautumn2025.util

import org.sonso.hackautumn2025.dto.User
import org.sonso.hackautumn2025.dto.response.RoleResponse
import org.sonso.hackautumn2025.entity.RoleEntity
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
