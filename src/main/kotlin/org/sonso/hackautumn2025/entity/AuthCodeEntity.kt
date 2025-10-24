package org.sonso.hackautumn2025.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "auth_codes")
data class AuthCodeEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(targetEntity = UserEntity::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    val userEntity: UserEntity? = null,

    @Column(name = "passcode", nullable = false)
    val passcode: String = "",

    @Column(name = "expire_date", nullable = false)
    val expireDate: Long = 0L,
)
