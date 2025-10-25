package org.sonso.hackautumn2025.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.sonso.hackautumn2025.dto.User
import org.sonso.hackautumn2025.dto.request.UserUpdateRequest
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.service.UserService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/user")
@Tag(name = "UserController", description = "Контроллер для взаимодействия с пользователями")
class UserController(
    private val userService: UserService
) {
    @PostMapping(
        "/upload-avatar",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @Operation(summary = "Загрузка аватарки", description = "Загружает аватарку пользователя в MinIO")
    fun uploadAvatar(
        @AuthenticationPrincipal user: UserEntity,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        val pathAvatar = userService.uploadAvatar(user, file)
        return ResponseEntity.ok(pathAvatar)
    }

    @PutMapping("/update")
    @Operation(summary = "Редактирование профиля", description = "Редактирование данных пользователя")
    fun update(
        @AuthenticationPrincipal user: UserEntity,
        @RequestBody userUpdate: UserUpdateRequest,
        response: HttpServletResponse,
    ): ResponseEntity<User> =
        ResponseEntity.ok(userService.update(user.id, userUpdate, response))


    @DeleteMapping("/delete")
    @Operation(summary = "Удаление аккаунта", description = "Удаление аккаунта")
    fun deleteById(
        @AuthenticationPrincipal user: UserEntity,
    ): ResponseEntity<User> =
        ResponseEntity.ok(userService.deleteById(user.id))

}
