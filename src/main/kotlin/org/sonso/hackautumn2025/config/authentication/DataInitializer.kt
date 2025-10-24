package org.sonso.hackautumn2025.config.authentication

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.properties.AuthenticationProperties
import org.sonso.hackautumn2025.properties.Role
import org.sonso.hackautumn2025.repository.RoleRepository
import org.sonso.hackautumn2025.repository.UserRepository
import java.time.LocalDateTime

@Component
class DataInitializer(
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationProperties: AuthenticationProperties
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(DataInitializer::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        log.info("Initializing admin user")

        val adminRole = roleRepository.findByName(Role.ADMIN)
            ?: throw IllegalStateException("Admin role not found. Please ensure migrations have been run.")

        val adminExists = userRepository.existsByEmail("admin@example.com")

        if (!adminExists) {
            log.info("Admin user not found, creating default admin")

            val user = UserEntity().apply {
                email = "admin@example.com"
                passwordHash = passwordEncoder.encode(authenticationProperties.adminPassword)
                role = adminRole
                nickname = "admin"
                createdAt = LocalDateTime.now()
            }

            userRepository.save(user)
            log.info("Default admin user created successfully")
        } else {
            log.info("Admin user already exists, skipping creation")
        }
    }
}
