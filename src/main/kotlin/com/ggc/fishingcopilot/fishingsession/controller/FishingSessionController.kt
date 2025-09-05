package com.ggc.fishingcopilot.fishingsession.controller

import com.ggc.fishingcopilot.fishingsession.model.dto.CreateFishingSessionRequest
import com.ggc.fishingcopilot.fishingsession.model.dto.FishingSessionResponse
import com.ggc.fishingcopilot.fishingsession.rod.FishingRodService
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.RodResponse
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.UpdateRodRequest
import com.ggc.fishingcopilot.fishingsession.service.FishingSessionService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class FishingSessionController(
    private val service: FishingSessionService,
    private val rodService: FishingRodService
) {

    @PostMapping("/fishing-session/create")
    @Operation(summary = "Create fishing session", description = "Create a new fishing session for the user")
    fun create(
        @RequestHeader("sessionId") sessionId: UUID,
        @RequestBody req: CreateFishingSessionRequest
    ): ResponseEntity<FishingSessionResponse> {
        val session = service.create(sessionId, req.name)
        return ResponseEntity.ok(FishingSessionResponse(session.id, session.name))
    }

    @GetMapping("/fishing-session/current")
    @Operation(summary = "Get current fishing session", description = "Returns current IN_PROGRESS session if exists")
    fun current(@RequestHeader("sessionId") sessionId: UUID): ResponseEntity<FishingSessionResponse> {
        val session = service.getCurrent(sessionId)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(FishingSessionResponse(session.id, session.name))
    }

    @PostMapping("/fishing-session/close")
    @Operation(summary = "Close current fishing session", description = "Close the current IN_PROGRESS session")
    fun close(@RequestHeader("sessionId") sessionId: UUID): ResponseEntity<Void> {
        service.close(sessionId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/fishing-session/{sessionId}/rods")
    @Operation(summary = "Add fishing rod", description = "Add a fishing rod to the session")
    fun addRod(
        @RequestHeader("sessionId") sessionId: UUID,
        @PathVariable("sessionId") fishingSessionId: Int
    ): ResponseEntity<RodResponse> {
        val rod = rodService.addRod(sessionId, fishingSessionId)
        return ResponseEntity.ok(RodResponse(rod.id, rod.fishCount))
    }

    @PatchMapping("/fishing-session/{sessionId}/rod/{rodId}")
    @Operation(summary = "Update rod fish count", description = "Update the fish count of a rod")
    fun updateRod(
        @RequestHeader("sessionId") sessionId: UUID,
        @PathVariable("sessionId") fishingSessionId: Int,
        @PathVariable rodId: Int,
        @RequestBody req: UpdateRodRequest
    ): ResponseEntity<RodResponse> {
        val rod = rodService.updateRod(sessionId, fishingSessionId, rodId, req.fishCount)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(RodResponse(rod.id, rod.fishCount))
    }

    @DeleteMapping("/fishing-session/{sessionId}/rod/{rodId}")
    @Operation(summary = "Delete fishing rod", description = "Delete a fishing rod from the session")
    fun deleteRod(
        @RequestHeader("sessionId") sessionId: UUID,
        @PathVariable("sessionId") fishingSessionId: Int,
        @PathVariable rodId: Int
    ): ResponseEntity<Void> {
        rodService.deleteRod(sessionId, fishingSessionId, rodId)
        return ResponseEntity.ok().build()
    }
}
