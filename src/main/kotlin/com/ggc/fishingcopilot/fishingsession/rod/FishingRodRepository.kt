package com.ggc.fishingcopilot.fishingsession.rod

import com.ggc.fishingcopilot.fishingsession.rod.model.entity.FishingRod
import org.springframework.data.jpa.repository.JpaRepository

interface FishingRodRepository : JpaRepository<FishingRod, Int> {
    fun findByIdAndFishingSessionId(id: Int, fishingSessionId: Int): FishingRod?
    fun deleteByIdAndFishingSessionId(id: Int, fishingSessionId: Int)
    fun findAllByFishingSessionId(fishingSessionId: Int): List<FishingRod>
}
