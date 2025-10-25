package org.sonso.hackautumn2025.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.sonso.hackautumn2025.dto.request.CreateRoomRequest
import org.sonso.hackautumn2025.dto.request.JoinRoomRequest
import org.sonso.hackautumn2025.dto.request.UpdateRoomRequest
import org.sonso.hackautumn2025.dto.response.JoinRoomResponse
import org.sonso.hackautumn2025.dto.response.RoomResponse
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.service.RoomService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "RoomController", description = "Контроллер для управления комнатами видеоконференций")
class RoomController(
    private val roomService: RoomService
) {

    @PostMapping
    @Operation(
        summary = "Создать комнату",
        description = "Создает новую комнату для видеоконференции. Создатель автоматически становится владельцем и участником."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Комната успешно создана"),
            ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
        ]
    )
    fun createRoom(
        @RequestBody request: CreateRoomRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal user: UserEntity
    ): ResponseEntity<UUID> {
        return ResponseEntity.ok(roomService.createRoom(request, user))
    }

    @GetMapping("/{roomId}")
    @Operation(
        summary = "Получить комнату по ID",
        description = "Возвращает информацию о комнате по её уникальному идентификатору"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Комната найдена"),
            ApiResponse(responseCode = "404", description = "Комната не найдена")
        ]
    )
    fun getRoomById(
        @Parameter(description = "Уникальный идентификатор комнаты", required = true)
        @PathVariable roomId: UUID
    ): ResponseEntity<RoomResponse> {
        val room = roomService.getRoomById(roomId)
        return ResponseEntity.ok(room)
    }

    @GetMapping
    @Operation(
        summary = "Получить все активные комнаты",
        description = "Возвращает список всех активных комнат, доступных для просмотра"
    )
    @ApiResponse(responseCode = "200", description = "Список активных комнат")
    fun getAllRooms(): ResponseEntity<List<RoomResponse>> {
        val rooms = roomService.getActiveRooms()
        return ResponseEntity.ok(rooms)
    }

    @GetMapping("/my")
    @Operation(
        summary = "Получить мои комнаты",
        description = "Возвращает список комнат, созданных текущим пользователем"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Список комнат пользователя"),
            ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
        ]
    )
    fun getMyRooms(
        @Parameter(hidden = true) @AuthenticationPrincipal user: UserEntity
    ): ResponseEntity<List<RoomResponse>> {
        val rooms = roomService.getUserRooms(user.id)
        return ResponseEntity.ok(rooms)
    }

    @PatchMapping("/{roomId}")
    @Operation(
        summary = "Обновить комнату",
        description = "Обновляет параметры комнаты. Доступно только владельцу комнаты."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Комната успешно обновлена"),
            ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            ApiResponse(responseCode = "403", description = "Недостаточно прав (не владелец комнаты)"),
            ApiResponse(responseCode = "404", description = "Комната не найдена")
        ]
    )
    fun updateRoom(
        @Parameter(description = "Уникальный идентификатор комнаты", required = true)
        @PathVariable roomId: UUID,
        @RequestBody request: UpdateRoomRequest,
        @Parameter(hidden = true) @AuthenticationPrincipal user: UserEntity
    ): ResponseEntity<RoomResponse> {
        val room = roomService.updateRoom(roomId, request, user.id)
        return ResponseEntity.ok(room)
    }

    @DeleteMapping("/{roomId}")
    @Operation(
        summary = "Удалить комнату",
        description = "Удаляет комнату. Доступно только владельцу комнаты."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Комната успешно удалена"),
            ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            ApiResponse(responseCode = "403", description = "Недостаточно прав (не владелец комнаты)"),
            ApiResponse(responseCode = "404", description = "Комната не найдена")
        ]
    )
    fun deleteRoom(
        @Parameter(description = "Уникальный идентификатор комнаты", required = true)
        @PathVariable roomId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal user: UserEntity
    ): ResponseEntity<Void> {
        roomService.deleteRoom(roomId, user.id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{roomId}/join")
    @Operation(
        summary = "Присоединиться к комнате",
        description = "Присоединяет пользователя к комнате. Для комнат с паролем требуется accessCode."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Успешно присоединились к комнате"),
            ApiResponse(responseCode = "400", description = "Неверный пароль или комната заполнена"),
            ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            ApiResponse(responseCode = "404", description = "Комната не найдена или неактивна")
        ]
    )
    fun joinRoom(
        @Parameter(description = "Уникальный идентификатор комнаты", required = true)
        @PathVariable roomId: UUID,
        @RequestBody(required = false) request: JoinRoomRequest?,
        @Parameter(hidden = true) @AuthenticationPrincipal user: UserEntity
    ): ResponseEntity<JoinRoomResponse> {
        val response = roomService.joinRoom(roomId, user.id, request?.accessCode)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{roomId}/leave")
    @Operation(
        summary = "Покинуть комнату",
        description = "Отмечает выход пользователя из комнаты"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Успешно покинули комнату"),
            ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            ApiResponse(responseCode = "404", description = "Пользователь не является участником комнаты")
        ]
    )
    fun leaveRoom(
        @Parameter(description = "Уникальный идентификатор комнаты", required = true)
        @PathVariable roomId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal user: UserEntity
    ): ResponseEntity<Void> {
        roomService.leaveRoom(roomId, user.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{roomId}/participants")
    @Operation(
        summary = "Получить участников комнаты",
        description = "Возвращает список идентификаторов активных участников комнаты"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Список участников"),
            ApiResponse(responseCode = "404", description = "Комната не найдена")
        ]
    )
    fun getRoomParticipants(
        @Parameter(description = "Уникальный идентификатор комнаты", required = true)
        @PathVariable roomId: UUID
    ): ResponseEntity<List<UUID>> {
        val participants = roomService.getRoomParticipants(roomId)
        return ResponseEntity.ok(participants)
    }
}
