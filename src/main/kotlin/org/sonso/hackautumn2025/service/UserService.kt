package org.sonso.hackautumn2025.service

import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.dto.User
import org.sonso.hackautumn2025.dto.request.UserUpdateRequest
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.repository.UserRepository
import org.sonso.hackautumn2025.util.exception.UserNotFoundException
import org.sonso.hackautumn2025.util.toUser
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*
import kotlin.NoSuchElementException
import kotlin.jvm.optionals.getOrElse

@Service
class UserService(
    private val minioService: MinioService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationService: AuthenticationService
) {
    val log = LoggerFactory.getLogger(UserService::class.java)

    @Transactional
    fun uploadAvatar(user: UserEntity, file: MultipartFile): String {
        log.info("Uploading avatar start")
        val userEnt = userRepository.findByEmail(user.email)
            ?: throw UserNotFoundException("Пользователь не найден")

        userEnt.avatarPath = minioService.uploadFile(user.id, file)

        userRepository.save(userEnt)

        log.info("Avatar uploaded successfully for user ${user.id}")
        return userEnt.avatarPath
    }

    @Transactional
    fun update(
        id: UUID,
        userUpdate: UserUpdateRequest,
        response: HttpServletResponse
    ): User {
        log.info("Update user start by id $id")
        val userEnt = userRepository.findById(id).getOrElse {
            throw NoSuchElementException("Пользователь не существует")
        }

        log.info("New user data: $userUpdate")
        userEnt.apply {
            userUpdate.nickname?.let { nickname = it }
            userUpdate.email?.let { email = it }
            userUpdate.password?.let {
                passwordHash = passwordEncoder.encode(it)
                authenticationService.logout(response)
            }
        }

        val result = userRepository.save(userEnt)

        log.info("Update user successfully")
        return result.toUser()
    }

    @Transactional
    fun deleteById(id: UUID): User{
        log.info("delete user start by id $id")
        val userEnt = userRepository.findById(id).getOrElse {
            throw NoSuchElementException("Пользователь не существует")
        }

        userRepository.deleteById(id)

        log.info("delete user success")
        return userEnt.toUser()
    }
}
