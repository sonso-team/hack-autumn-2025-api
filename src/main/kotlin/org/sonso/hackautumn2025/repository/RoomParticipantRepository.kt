package org.sonso.hackautumn2025.repository

import org.sonso.hackautumn2025.entity.RoomParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoomParticipantRepository : JpaRepository<RoomParticipantEntity, UUID> {
    fun findByRoomId(roomId: UUID): List<RoomParticipantEntity>
    fun findByRoomIdAndUserId(roomId: UUID, userId: UUID): Optional<RoomParticipantEntity>
    fun findByUserIdAndLeftAtIsNull(userId: UUID): List<RoomParticipantEntity>
}
