package org.sonso.hackautumn2025.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.sonso.hackautumn2025.entity.RoleEntity
import java.util.UUID

@Repository
interface RoleRepository : JpaRepository<RoleEntity, UUID> {
    fun findByName(name: String): RoleEntity?
}
