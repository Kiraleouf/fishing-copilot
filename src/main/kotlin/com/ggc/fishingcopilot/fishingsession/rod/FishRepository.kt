package com.ggc.fishingcopilot.fishingsession.rod

import com.ggc.fishingcopilot.fishingsession.rod.model.entity.Fish
import org.springframework.data.jpa.repository.JpaRepository

interface FishRepository : JpaRepository<Fish, Int> {
    fun findTopByFishingRodIdOrderByCaughtAtDesc(fishingRodId: Int): Fish?
    fun countByFishingRodId(fishingRodId: Int): Int
}
