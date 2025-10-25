package org.sonso.hackautumn2025.dto

import java.time.LocalDate
import java.util.*

data class HistoryUnit(
    val name: String,
    val date: LocalDate,
    val uuid: UUID,
)
