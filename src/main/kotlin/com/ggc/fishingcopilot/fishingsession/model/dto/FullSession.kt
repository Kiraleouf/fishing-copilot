package com.ggc.fishingcopilot.fishingsession.model.dto

import com.ggc.fishingcopilot.fishingsession.rod.model.dto.RodResponse
import java.time.LocalDate
import java.util.UUID


data class FullSession(
    val id: UUID,
    val name: String,
    val date: LocalDate,
    val rods: List<RodResponse>,
    val pictures: List<PictureDto>
)