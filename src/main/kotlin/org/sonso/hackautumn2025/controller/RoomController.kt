package org.sonso.hackautumn2025.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.dto.HistoryUnit
import org.sonso.hackautumn2025.dto.request.CreateRoomRequest
import org.sonso.hackautumn2025.dto.request.JoinRoomRequest
import org.sonso.hackautumn2025.dto.request.UpdateRoomRequest
import org.sonso.hackautumn2025.dto.response.JoinRoomResponse
import org.sonso.hackautumn2025.dto.response.RoomResponse
import org.sonso.hackautumn2025.entity.UserEntity
import org.sonso.hackautumn2025.service.RoomService
import org.sonso.hackautumn2025.websocket.CallSocketHandler
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "RoomController", description = "Контроллер для управления комнатами видеоконференций")
class RoomController(
    private val roomService: RoomService,
    private val callSocketHandler: CallSocketHandler
) {
    private val logger = LoggerFactory.getLogger(RoomController::class.java)

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

    @PostMapping("/delete/{roomId}")
    @Operation(
        summary = "Завершить конференцию",
        description = "Закрытие комнаты, отключение всех участников"
    )
    fun delete(
        @Parameter(description = "Уникальный идентификатор комнаты", required = true)
        @PathVariable roomId: UUID,
        @Parameter(hidden = true) @AuthenticationPrincipal user: UserEntity
    ): ResponseEntity<RoomResponse> {
        // Уведомление websocket-участников о завершении
        callSocketHandler.closeConference (roomId.toString(), user.id)

        return ResponseEntity.ok(roomService.deleteRoom(roomId, user.id))
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

    @GetMapping("/closed")
    @Operation(summary = "Выкачка отчета о транзакциях пользователей")
    fun getAllClosedConferenceList(
        @RequestHeader(value = "Authorization") token: String
    ): ResponseEntity<List<HistoryUnit>> {
        logger.info("Request to all closed conference list")
        return ResponseEntity.ok(roomService.getAllClosedConferenceList(token))
    }

    @GetMapping("/closed/download/{id}")
    @Operation(summary = "Выкачка отчета о транзакциях пользователей")
    fun downloadTransactionsReport(@PathVariable id: String): ResponseEntity<ByteArray> {
        logger.info("Request download history unit")
        val room = roomService.getRoomById(id)
        val history = requireNotNull(room.history) {
            "This conf is not ended or history not found"
        }
        val headers = HttpHeaders()
        headers.add(
            "Content-Disposition", "attachment; filename=${room.name}-${room.createdAt}.json"
        )
        headers.add("Content-Type", "text/plain; charset=UTF-8")
        return ResponseEntity(history.toByteArray(Charsets.UTF_8), headers, HttpStatus.OK)
    }
}
