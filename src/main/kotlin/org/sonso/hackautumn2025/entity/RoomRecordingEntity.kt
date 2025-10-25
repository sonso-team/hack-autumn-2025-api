package org.sonso.hackautumn2025.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "room_recordings")
class RoomRecordingEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID()

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    lateinit var room: RoomEntity

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "recorded_by")
    var recordedBy: UserEntity? = null

    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null

    @Column(name = "ended_at")
    var endedAt: LocalDateTime? = null

    @Column(name = "file_url", columnDefinition = "TEXT")
    var fileUrl: String? = null
}
