package org.sonso.hackautumn2025.entity

import jakarta.persistence.*
import java.util.*


@Entity
@Table(name = "roles")
class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID = UUID.randomUUID()

    @Column(name = "name", nullable = false)
    var name: String = ""

    @OneToMany(mappedBy = "role")
    var users: MutableSet<UserEntity> = mutableSetOf()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   name = $name )"
    }
}
