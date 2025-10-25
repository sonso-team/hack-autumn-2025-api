package org.sonso.hackautumn2025.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.dto.HistoryUnit
import org.sonso.hackautumn2025.service.RoomService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@Controller("/api/rooms")
class RoomController(
    private val roomService: RoomService,
) {
    private val logger = LoggerFactory.getLogger(RoomController::class.java)

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