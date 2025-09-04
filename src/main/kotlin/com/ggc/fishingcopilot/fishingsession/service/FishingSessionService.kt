package com.ggc.fishingcopilot.fishingsession.service

import com.ggc.fishingcopilot.fishingsession.FishingSessionRepository
import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession
import com.ggc.fishingcopilot.session.UserSessionRepository
import com.ggc.fishingcopilot.fisherman.exception.SessionNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FishingSessionService(
    private val sessionRepository: UserSessionRepository,
    private val fishingSessionRepository: FishingSessionRepository
) {
    fun create(sessionId: UUID, name: String?): FishingSession {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val sessionName = name?.takeIf { it.isNotBlank() } ?: "sans nom"
        val fishingSession = FishingSession(name = sessionName, fisherman = session.fisherman)
        return fishingSessionRepository.save(fishingSession)
    }
}
