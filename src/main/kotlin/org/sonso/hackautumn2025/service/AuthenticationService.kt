package org.sonso.hackautumn2025.service

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.sonso.hackautumn2025.dto.User
import org.sonso.hackautumn2025.dto.request.AuthenticationRequest
import org.sonso.hackautumn2025.dto.request.RegistrationRequest
import org.sonso.hackautumn2025.dto.request.SendCodeRequest
import org.sonso.hackautumn2025.dto.response.AuthenticationResponse
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.properties.AuthenticationProperties
import org.sonso.hackautumn2025.properties.Role.GUEST
import org.sonso.hackautumn2025.repository.AuthCodeRepository
import org.sonso.hackautumn2025.repository.RoleRepository
import org.sonso.hackautumn2025.repository.UserRepository
import org.sonso.hackautumn2025.util.exception.AuthenticationException
import org.sonso.hackautumn2025.util.exception.UserNotFoundException
import org.sonso.hackautumn2025.util.toUser
import java.time.Duration
import java.time.LocalDateTime

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val mailService: MailService,
    private val authCodeRepository: AuthCodeRepository,
    private val authenticationProperties: AuthenticationProperties,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository
) {
    private val log: Logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    @Transactional
    fun authorization(request: AuthenticationRequest): AuthenticationResponse {
        if (request.login.isEmpty() || request.password.isEmpty())
            throw AuthenticationException("Поля логин и/или пароль пустые")

        val userEntity = userRepository.findByEmail(request.login)
            ?: throw UserNotFoundException("Пользователь не найден")

        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(request.login, request.password))
        log.info("login and password correct")

        mailService.sendPassCode(userEntity)

        return AuthenticationResponse(
            message = "Для окончания входа введите код отправленный на почту",
            user = userEntity.toUser()
        )
    }

    private fun performLogin(userEntity: UserEntity, response: HttpServletResponse): AuthenticationResponse {
        val (access, refresh) = jwtService.generateTokens(userEntity)
        setRefreshToken(response, refresh)

        log.debug("User {} has been generate tokens", userEntity.id)
        log.info("Authorization successfully")

        return AuthenticationResponse(
            message = "Авторизация прошла успешно",
            token = access,
            user = userEntity.toUser()
        )
    }

    @Transactional
    fun sendCode(request: SendCodeRequest): Map<String, String> {
        val userEntity = userRepository.findByEmail(request.login)
            ?: throw UserNotFoundException("Пользователь не найден")

        mailService.sendPassCode(userEntity)
        return mapOf("message" to "Код успешно выслан на почту ${userEntity.email}")
    }

    @Transactional
    fun checkAuthCode(request: AuthenticationRequest, response: HttpServletResponse): AuthenticationResponse {
        if (request.login.isEmpty() || request.password.isEmpty())
            throw AuthenticationException("Поля логин и/или пароль пустые")

        val userEntity = userRepository.findByEmail(request.login)
            ?: throw UserNotFoundException("Пользователь не найден")
        if (request.password == authenticationProperties.adminCode) return performLogin(userEntity, response)

        val passwordEntities = authCodeRepository.findAuthCodeEntitiesByUserEntity(userEntity)

        if (passwordEntities.all { it.passcode.isEmpty() })
            throw AuthenticationException("Доступных паролей для пользователя ${userEntity.id} не найдено")

        if (!passwordEntities.map { it.passcode }.contains(request.password))
            throw AuthenticationException("Неверный одноразовый код. Попробуйте еще раз или запросите новый")

        passwordEntities.forEach { passwordEntity ->
            if (passwordEntity.passcode == request.password) {
                if (passwordEntity.expireDate >= System.currentTimeMillis()) {
                    authCodeRepository.deleteAuthCodeEntitiesByUserEntity(userEntity)
                    return@forEach
                } else throw AuthenticationException("Одноразовый код для входа устарел. Попробуйте запросить другой")
            }
        }

        return performLogin(userEntity, response)
    }

    @Transactional
    fun registration(request: RegistrationRequest, roleName: String = GUEST): AuthenticationResponse {
        validateCredentials(request)

        if (userRepository.findByEmail(request.email) != null)
            throw AuthenticationException("Пользователь с таким адресом электронной почты уже существует")

        val roleEntity = roleRepository.findByName(roleName)!!
        val userEntity = userRepository.save(
            UserEntity().apply {
                email = request.email
                nickname = request.nickname
                passwordHash = passwordEncoder.encode(request.password)
                createdAt = LocalDateTime.now()
                role = roleEntity
            }
        )
        log.debug("User {} successful saved in database", userEntity.id)

        mailService.sendPassCode(userEntity)

        log.debug("User {} has been register", userEntity.id)
        log.info("Registration is successful, send code to email")
        return AuthenticationResponse(
            message =
            "Пользователь зарегистрирован. Для окончания регистрации и входа введите код отправленный на почту",
            user = userEntity.toUser()
        )
    }

    fun logout(response: HttpServletResponse): Map<String, String> {
        val cookie = Cookie("refreshToken", null)
        cookie.maxAge = 0
        cookie.path = "/"
        response.addCookie(cookie)

        return mapOf("message" to "Выход из аккаунта прошел успешно")
    }

    fun refresh(token: String, response: HttpServletResponse): AuthenticationResponse {
        if (token.isEmpty()) {
            log.warn("Token is empty")
            throw AuthenticationException("Токен пуст")
        }

        val userEntity = userRepository.findByEmail(jwtService.getUsername(token.substring(7)))
            ?: throw UserNotFoundException("Пользователь не существует")

        val (access, refresh) = jwtService.generateTokens(userEntity)

        setRefreshToken(response, refresh)

        log.debug("Token for user {} has been refreshed", userEntity.id)

        return AuthenticationResponse(
            message = "Токены успешно обновлены",
            token = access,
            user = userEntity.toUser()
        )
    }

    fun userInfoUser(user: UserEntity): User {
        log.info("WhoAmI for user ${user.id} successful")
        return user.toUser()
    }

    fun setRefreshToken(response: HttpServletResponse, token: String) {
        val cookie = ResponseCookie.from("refreshToken", "Bearer_$token")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(30))
            .sameSite("None")
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private fun validateCredentials(request: RegistrationRequest) {
        val notValid = request.email.isEmpty() ||
            request.nickname.isEmpty() ||
            request.password.isEmpty()

        if (notValid) throw AuthenticationException("Необходимые поля для регистрации пустые")
    }
}
