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
@Table(name = "room_files")
class RoomFileEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID()

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    lateinit var room: RoomEntity

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    lateinit var sender: UserEntity

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    var fileUrl: String = ""

    @Column(name = "uploaded_at")
    var uploadedAt: LocalDateTime? = null

    @Column(name = "mime_type", length = 100)
    var mimeType: String? = null
}
