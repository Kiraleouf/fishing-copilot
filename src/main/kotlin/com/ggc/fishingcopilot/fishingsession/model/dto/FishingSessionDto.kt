package com.ggc.fishingcopilot.fishingsession.model.dto

import java.time.LocalDate

data class CreateFishingSessionRequest(
    val name: String? = null
)

data class FishingSessionResponse(
    val id: Int,
    val name: String,
    val date: LocalDate
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

