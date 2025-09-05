package com.ggc.fishingcopilot.fishingsession.rod

import com.ggc.fishingcopilot.fishingsession.FishingSessionRepository
import com.ggc.fishingcopilot.fishingsession.rod.model.entity.FishingRod
import com.ggc.fishingcopilot.session.UserSessionRepository
import com.ggc.fishingcopilot.fisherman.exception.SessionNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FishingRodService(
    private val sessionRepository: UserSessionRepository,
    private val fishingSessionRepository: FishingSessionRepository,
    private val rodRepository: FishingRodRepository
) {
    fun addRod(sessionId: UUID, fishingSessionId: Int): FishingRod {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        val rod = FishingRod(fishingSession = fishingSession)
        return rodRepository.save(rod)
    }

    fun updateRod(sessionId: UUID, fishingSessionId: Int, rodId: Int, fishCount: Int): FishingRod? {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        val rod = rodRepository.findByIdAndFishingSessionId(rodId, fishingSession.id) ?: return null
        rod.fishCount = fishCount
        return rodRepository.save(rod)
    }

    fun deleteRod(sessionId: UUID, fishingSessionId: Int, rodId: Int) {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        rodRepository.deleteByIdAndFishingSessionId(rodId, fishingSession.id)
    }
}
