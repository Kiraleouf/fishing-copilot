package com.ggc.fishingcopilot.fishingsession.service

import com.ggc.fishingcopilot.fishingsession.FishingSessionRepository
import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession
import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSessionStatus
import com.ggc.fishingcopilot.session.UserSessionRepository
import com.ggc.fishingcopilot.fisherman.exception.SessionNotFoundException
import com.ggc.fishingcopilot.fishingsession.PictureRepository
import com.ggc.fishingcopilot.fishingsession.mapper.FishingSessionMapper
import com.ggc.fishingcopilot.fishingsession.model.dto.FishingSessionResponse
import com.ggc.fishingcopilot.fishingsession.model.dto.FullSession
import com.ggc.fishingcopilot.fishingsession.model.dto.PaginatedResponse
import com.ggc.fishingcopilot.fishingsession.rod.FishRepository
import com.ggc.fishingcopilot.fishingsession.rod.FishingRodRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FishingSessionService(
    private val sessionRepository: UserSessionRepository,
    private val fishingSessionRepository: FishingSessionRepository,
    private val fishingRodRepository: FishingRodRepository,
    private val fishRepository: FishRepository,
    private val pictureRepository: PictureRepository,
    private val mapper: FishingSessionMapper
) {
    fun create(sessionId: UUID, name: String?): FishingSession {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val sessionName = name?.takeIf { it.isNotBlank() } ?: "sans nom"
        val fishingSession = FishingSession(name = sessionName, fisherman = session.fisherman)
        return fishingSessionRepository.save(fishingSession)
    }

    fun getCurrent(sessionId: UUID): FishingSession? {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        return fishingSessionRepository.findFirstByFishermanAndStatus(
            session.fisherman,
            FishingSessionStatus.IN_PROGRESS
        )
    }

    fun getPaginatedSessions(sessionId: UUID, page: Int, size: Int): PaginatedResponse<FishingSessionResponse> {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val result = fishingSessionRepository.findAllByFisherman(session.fisherman, pageable)

        val items = result.content.map {
            FishingSessionResponse(
                id = it.id,
                name = it.name,
                date = it.date
            )
        }

        return PaginatedResponse(
            items = items,
            page = result.number,
            size = result.size,
            totalItems = result.totalElements,
            totalPages = result.totalPages,
            hasNext = result.hasNext(),
            hasPrevious = result.hasPrevious()
        )
    }

    fun close(sessionId: UUID) {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val current = fishingSessionRepository.findFirstByFishermanAndStatus(
            session.fisherman,
            FishingSessionStatus.IN_PROGRESS
        ) ?: return
        current.status = FishingSessionStatus.CLOSED
        fishingSessionRepository.save(current)
    }

    fun getFullSession(sessionId: UUID, fishingSessionId: Int): FullSession {
        val session = sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }
        val fishingSession = fishingSessionRepository.findById(fishingSessionId).orElseThrow{ IllegalArgumentException("Fishing session not found") }
        if (fishingSession.fisherman.id != session.fisherman.id) {
            throw IllegalAccessException("You do not have access to this fishing session")
        }
        val rods = fishingRodRepository.findAllByFishingSessionId(fishingSessionId)
        val fishes = fishRepository.findByFishingRodIdIn(rods.map { it.id })
        val pictures = pictureRepository.findByFishingSessionId(fishingSessionId).map { mapper.mapPictureToDto(it) }
        val fullSession = mapper.mapToFullSessionDto(mapper.mapRodsAndFishToRodResponse(rods, fishes) , pictures, sessionId, fishingSession.name, fishingSession.date)
        return fullSession
    }
}
