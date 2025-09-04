package com.ggc.fishingcopilot.fishingsession

import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession
import org.springframework.data.jpa.repository.JpaRepository

interface FishingSessionRepository : JpaRepository<FishingSession, Long>
