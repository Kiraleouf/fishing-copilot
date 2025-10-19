package com.ggc.fishingcopilot.fishingsession

import com.ggc.fishingcopilot.fishingsession.model.entity.Picture
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PictureRepository : JpaRepository<Picture, UUID> {
    fun findByFishingSessionId(fishingSessionId: Int): List<Picture>
    fun countByFishingSessionId(fishingSessionId: Int): Int
}
