package org.sonso.hackautumn2025.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "rooms")
class RoomEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID()

    @Column(name = "name", nullable = false, length = 255)
    var name: String = ""

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    lateinit var owner: UserEntity

    @Column(name = "status", length = 20)
    var status: String? = null

    @Column(name = "type", nullable = false, length = 30)
    var type: String = ""

    @Column(name = "access_code", length = 255)
    var accessCode: String? = null

    @Column(name = "max_participants")
    var maxParticipants: Int? = null

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @Column(name = "description", length = 200)
    var description: String? = null

    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var participants: MutableSet<RoomParticipantEntity> = mutableSetOf()

    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var files: MutableSet<RoomFileEntity> = mutableSetOf()

    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var recordings: MutableSet<RoomRecordingEntity> = mutableSetOf()
}
