package org.sonso.hackautumn2025.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.service.UserService

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
}