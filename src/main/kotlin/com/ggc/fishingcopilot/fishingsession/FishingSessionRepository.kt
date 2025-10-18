package com.ggc.fishingcopilot.fishingsession

import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession
import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSessionStatus
import com.ggc.fishingcopilot.fisherman.model.entity.Fisherman
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FishingSessionRepository : JpaRepository<FishingSession, Int> {
    fun findFirstByFishermanAndStatus(fisherman: Fisherman, status: FishingSessionStatus): FishingSession?

    fun findAllByFisherman(fisherman: Fisherman, pageable: Pageable): Page<FishingSession>
}
