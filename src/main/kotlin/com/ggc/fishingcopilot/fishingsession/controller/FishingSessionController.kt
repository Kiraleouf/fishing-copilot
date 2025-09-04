package com.ggc.fishingcopilot.fishingsession.controller

import com.ggc.fishingcopilot.fishingsession.model.dto.CreateFishingSessionRequest
import com.ggc.fishingcopilot.fishingsession.model.dto.FishingSessionResponse
import com.ggc.fishingcopilot.fishingsession.service.FishingSessionService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class FishingSessionController(private val service: FishingSessionService) {

    @PostMapping("/fishing-session/create")
    @Operation(summary = "Create fishing session", description = "Create a new fishing session for the user")
    fun create(
        @RequestHeader("sessionId") sessionId: UUID,
        @RequestBody req: CreateFishingSessionRequest
    ): ResponseEntity<FishingSessionResponse> {
        val session = service.create(sessionId, req.name)
        return ResponseEntity.ok(FishingSessionResponse(session.id, session.name))
    }
}
