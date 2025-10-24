package org.sonso.hackautumn2025.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.sonso.hackautumn2025.entity.AuthCodeEntity
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.properties.AuthenticationProperties
import org.sonso.hackautumn2025.repository.AuthCodeRepository

@Service
class MailService(
    private val passwordsRepository: AuthCodeRepository,
    private val authenticationProperties: AuthenticationProperties,
    private val emailSender: EmailSender
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    internal fun sendPassCode(userEntity: UserEntity) {
        log.info("Generation verification code")
        val passcode = (100000..999999).random().toString()

        val authCodeEntity = AuthCodeEntity(
            userEntity = userEntity,
            passcode = passcode,
            expireDate = System.currentTimeMillis() + authenticationProperties.passwordLifeTime
        )
        log.info("Verification code for user ${userEntity.id} has been generated")
        log.debug("Verification code for user {}: {}", userEntity.id, passcode)
        passwordsRepository.save(authCodeEntity)

        emailSender.sendPassCodeMessage(userEntity.email, passcode)
    }
}
