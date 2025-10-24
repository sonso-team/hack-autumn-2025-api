package org.sonso.hackautumn2025.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.sonso.hackautumn2025.entity.UserEntity
import java.util.*

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun findByAvatarPath(avatarPath: String): UserEntity?
}
