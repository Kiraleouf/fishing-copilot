package com.ggc.fishingcopilot.fishingsession.rod.model.dto

data class CreateRodRequest(
    val name: String
)

data class RodResponse(
    val id: Int,
    val name: String,
    val fishCount: Int
)
