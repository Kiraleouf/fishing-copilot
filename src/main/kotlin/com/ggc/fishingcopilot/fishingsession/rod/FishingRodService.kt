package com.ggc.fishingcopilot.fishingsession.rod

import com.ggc.fishingcopilot.fishingsession.FishingSessionRepository
import com.ggc.fishingcopilot.fishingsession.rod.model.entity.FishingRod
import com.ggc.fishingcopilot.fishingsession.rod.model.entity.Fish
import com.ggc.fishingcopilot.session.UserSessionRepository
import com.ggc.fishingcopilot.fisherman.exception.SessionNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FishingRodService(
    private val sessionRepository: UserSessionRepository,
    private val fishingSessionRepository: FishingSessionRepository,
    private val rodRepository: FishingRodRepository,
    private val fishRepository: FishRepository
) {
    fun addRod(sessionId: UUID, fishingSessionId: Int): FishingRod {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        val rod = FishingRod(fishingSession = fishingSession)
        return rodRepository.save(rod)
    }

    fun deleteRod(sessionId: UUID, fishingSessionId: Int, rodId: Int) {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        rodRepository.deleteByIdAndFishingSessionId(rodId, fishingSession.id)
    }

    fun addFish(sessionId: UUID, fishingSessionId: Int, rodId: Int): Int? {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        val rod = rodRepository.findByIdAndFishingSessionId(rodId, fishingSession.id) ?: return null
        fishRepository.save(Fish(fishingRod = rod))
        return fishRepository.countByFishingRodId(rod.id)
    }

    fun removeFish(sessionId: UUID, fishingSessionId: Int, rodId: Int): Int? {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        val rod = rodRepository.findByIdAndFishingSessionId(rodId, fishingSession.id) ?: return null
        val fish = fishRepository.findTopByFishingRodIdOrderByCaughtAtDesc(rod.id) ?: return fishRepository.countByFishingRodId(rod.id)
        fishRepository.delete(fish)
        return fishRepository.countByFishingRodId(rod.id)
    }
}
