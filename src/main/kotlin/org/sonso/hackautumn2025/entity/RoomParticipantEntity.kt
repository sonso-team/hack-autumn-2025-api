package org.sonso.hackautumn2025.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "room_participants",
    uniqueConstraints = [UniqueConstraint(name = "uk_room_user", columnNames = ["room_id", "user_id"])]
)
class RoomParticipantEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID()

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    lateinit var room: RoomEntity

    // В схеме FK на users для user_id не указан — маппим как UUID поле.
    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID()

    @Column(name = "joined_at")
    var joinedAt: LocalDateTime? = null

    @Column(name = "left_at")
    var leftAt: LocalDateTime? = null
}
