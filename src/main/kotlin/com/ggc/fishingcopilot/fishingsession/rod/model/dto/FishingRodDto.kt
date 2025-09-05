package com.ggc.fishingcopilot.fishingsession.rod.model.dto

data class RodResponse(
    val id: Int,
    val fishCount: Int
)

data class UpdateRodRequest(
    val fishCount: Int
)
