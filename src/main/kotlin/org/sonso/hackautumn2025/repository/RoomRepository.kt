package org.sonso.hackautumn2025.repository

import org.sonso.hackautumn2025.entity.RoomEntity
import org.sonso.hackautumn2025.entity.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoomRepository: CrudRepository<RoomEntity, UUID> {
    fun findAllByHistoryNotNullAndOwner(owner: UserEntity): List<RoomEntity>
    fun findRoomEntityById(id: UUID): RoomEntity?
}
