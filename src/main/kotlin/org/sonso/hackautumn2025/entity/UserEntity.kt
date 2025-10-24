package org.sonso.hackautumn2025.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "users")
class UserEntity :  UserDetails {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    val id: UUID = UUID.randomUUID()

    @Column(unique = true, nullable = false)
    var email: String = ""

    @Column(name = "password", nullable = false)
    var passwordHash: String = ""

    @Column(nullable = false)
    var nickname: String = ""

    @Column(name="avatar_path")
    var avatarPath: String = ""

    @Column(nullable = false)
    lateinit var createdAt: LocalDateTime

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    lateinit var role: RoleEntity

    override fun getUsername() = this.email
    override fun getPassword() = this.passwordHash
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority(role.name))

}
