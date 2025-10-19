package com.ggc.fishingcopilot.fishingsession.mapper

import com.ggc.fishingcopilot.fishingsession.model.dto.FullSession
import com.ggc.fishingcopilot.fishingsession.model.dto.PictureDto
import com.ggc.fishingcopilot.fishingsession.model.entity.Picture
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.RodResponse
import com.ggc.fishingcopilot.fishingsession.rod.model.entity.Fish
import com.ggc.fishingcopilot.fishingsession.rod.model.entity.FishingRod
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Component
class FishingSessionMapper {
    //Add static method to  map Picture to PictureDto
    fun mapPictureToDto(picture: Picture) = PictureDto(
        imgPath = picture.fishingSession?.id.toString() + "/" + picture.name
    )

    //add method to map list of rods to list of RodResponse
    fun mapRodsAndFishToRodResponse(rodList: List<FishingRod>, fishes: List<Fish>) =
        rodList.map { rod ->
            RodResponse(
                id = rod.id,
                name = rod.name,
                fishCount = fishes.count { fish -> fish.fishingRod.id == rod.id }
            )
        }

    fun mapToFullSessionDto(rods: List<RodResponse>, pictures: List<PictureDto>, sessionId: UUID, sessionName: String, sessionDate: LocalDate) =
        FullSession(
            id = sessionId,
            name = sessionName,
            date = sessionDate,
            rods = rods,
            pictures = pictures
        )
}