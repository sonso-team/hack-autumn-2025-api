package org.sonso.hackautumn2025.service

import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.exceptions.UserNotFoundException
import org.sonso.hackautumn2025.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    private val minioService: MinioService,
    private val userRepository: UserRepository
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
}
