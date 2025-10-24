package org.sonso.hackautumn2025.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.sonso.hackautumn2025.entity.AuthCodeEntity
import org.sonso.hackautumn2025.entity.UserEntity
import java.util.UUID

interface AuthCodeRepository : JpaRepository<AuthCodeEntity, UUID> {

    fun findAuthCodeEntitiesByUserEntity(userEntity: UserEntity): List<AuthCodeEntity>
    fun deleteAuthCodeEntitiesByUserEntity(userEntity: UserEntity)
}
