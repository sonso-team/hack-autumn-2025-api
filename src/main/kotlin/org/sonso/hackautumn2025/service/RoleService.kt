package org.sonso.hackautumn2025.service

import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.dto.request.RoleRequest
import org.sonso.hackautumn2025.dto.response.RoleResponse
import org.sonso.hackautumn2025.entity.RoleEntity
import org.sonso.hackautumn2025.repository.RoleRepository
import org.sonso.hackautumn2025.util.toResponseEventDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {
    private val log = LoggerFactory.getLogger(RoleService::class.java)

    fun getById(id: UUID): RoleResponse {
        log.info("Fetching role by ID: $id")
        return roleRepository.findById(id)
            .orElseThrow {
                log.error("Role with ID $id not found")
                NoSuchElementException("Role with ID $id not found")
            }
            .toResponseEventDto()
    }

    fun getAll(): List<RoleResponse> {
        log.info("Fetching all roles")
        return roleRepository.findAll().map { it.toResponseEventDto() }
    }

    @Transactional
    fun save(roleName: RoleRequest): RoleResponse {
        log.info("Saving new role: $roleName")

        return roleRepository.save(
            RoleEntity().apply{name = roleName.roleName}
        ).toResponseEventDto().also {
            log.info("Role saved successfully: $roleName")
        }
    }

    fun delete(id: UUID): RoleResponse {
        log.info("Deleting role by ID: $id")
        val existingRole = roleRepository.findById(id)
            .orElseThrow {
                log.error("Role with ID $id not found")
                IllegalArgumentException("Role with ID $id not found")
            }

        roleRepository.delete(existingRole)
        log.info("Role deleted successfully: $id")
        return existingRole.toResponseEventDto()
    }

}