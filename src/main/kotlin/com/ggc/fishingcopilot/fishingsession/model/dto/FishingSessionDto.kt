package com.ggc.fishingcopilot.fishingsession.model.dto

data class CreateFishingSessionRequest(
    val name: String? = null
)

data class FishingSessionResponse(
    val id: Long,
    val name: String
)
