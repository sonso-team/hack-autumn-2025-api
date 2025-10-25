package org.sonso.hackautumn2025.service

import org.slf4j.LoggerFactory
import org.sonso.hackautumn2025.repository.RoomRepository
import org.sonso.hackautumn2025.repository.UserRepository
import org.sonso.hackautumn2025.util.toHistoryUnit
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) {
    private val logger = LoggerFactory.getLogger(RoomService::class.java)

    fun getAllClosedConferenceList(token: String) = roomRepository
        .findAllByHistoryNotNullAndOwner(
            requireNotNull(
                userRepository.findUserEntityById(
                    UUID.fromString(jwtService.getId(token))
                )
            ) {
                "User is not real. It's mystery. WHAT A HELL WHERE IS HE??? OH MY GOOOOOD"
            }
        )
        .map {
            it.toHistoryUnit()
        }

    fun getRoomById(id: String) = requireNotNull(
        roomRepository.findRoomEntityById(
            UUID.fromString(id)
        )
    ) {
        "Room is not exist"
    }
}
