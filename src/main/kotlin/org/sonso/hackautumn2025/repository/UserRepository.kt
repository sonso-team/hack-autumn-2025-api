package org.sonso.hackautumn2025.repository

import org.sonso.hackautumn2025.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findUserEntityById(id: UUID): UserEntity?
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun findByAvatarPath(avatarPath: String): UserEntity?
}
