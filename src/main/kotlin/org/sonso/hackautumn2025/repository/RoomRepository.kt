package org.sonso.hackautumn2025.repository

import org.sonso.hackautumn2025.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoomRepository : JpaRepository<RoomEntity, UUID> {
    fun findByOwnerId(ownerId: UUID): List<RoomEntity>
    fun findByStatus(status: String): List<RoomEntity>
    fun findByIdAndStatus(id: UUID, status: String): Optional<RoomEntity>
}
