package com.ggc.fishingcopilot.fishingsession.rod

import com.ggc.fishingcopilot.fishingsession.FishingSessionRepository
import com.ggc.fishingcopilot.fishingsession.rod.model.entity.FishingRod
import com.ggc.fishingcopilot.fishingsession.rod.model.entity.Fish
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.RodResponse
import com.ggc.fishingcopilot.session.UserSessionRepository
import com.ggc.fishingcopilot.fisherman.exception.SessionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FishingRodService(
    private val sessionRepository: UserSessionRepository,
    private val fishingSessionRepository: FishingSessionRepository,
    private val rodRepository: FishingRodRepository,
    private val fishRepository: FishRepository
) {
    fun addRod(sessionId: UUID, fishingSessionId: Int, name: String): FishingRod {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        val rod = FishingRod(name = name.take(20), fishingSession = fishingSession)
        return rodRepository.save(rod)
    }

    @Transactional
    fun deleteRod(sessionId: UUID, fishingSessionId: Int, rodId: Int) {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        rodRepository.deleteByIdAndFishingSessionId(rodId, fishingSession.id)
    }

    fun getRods(sessionId: UUID, fishingSessionId: Int): List<RodResponse> {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        return rodRepository.findAllByFishingSessionId(fishingSession.id).map { rod ->
            val count = fishRepository.countByFishingRodId(rod.id)
            RodResponse(rod.id, rod.name, count)
        }
    }

    fun addFish(sessionId: UUID, fishingSessionId: Int, rodId: Int): RodResponse? {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        val rod = rodRepository.findByIdAndFishingSessionId(rodId, fishingSession.id) ?: return null
        fishRepository.save(Fish(fishingRod = rod))
        val count = fishRepository.countByFishingRodId(rod.id)
        return RodResponse(rod.id, rod.name, count)
    }

    fun removeFish(sessionId: UUID, fishingSessionId: Int, rodId: Int): RodResponse? {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .filter { it.fisherman == session.fisherman }
            .orElseThrow { SessionNotFoundException() }
        val rod = rodRepository.findByIdAndFishingSessionId(rodId, fishingSession.id) ?: return null
        val fish = fishRepository.findTopByFishingRodIdOrderByCaughtAtDesc(rod.id)
            ?: return RodResponse(rod.id, rod.name, fishRepository.countByFishingRodId(rod.id))
        fishRepository.delete(fish)
        val count = fishRepository.countByFishingRodId(rod.id)
        return RodResponse(rod.id, rod.name, count)
    }
}
