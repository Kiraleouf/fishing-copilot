package com.ggc.fishingcopilot.fishingsession.controller

import com.ggc.fishingcopilot.fishingsession.model.dto.CreateFishingSessionRequest
import com.ggc.fishingcopilot.fishingsession.model.dto.FishingSessionResponse
import com.ggc.fishingcopilot.fishingsession.model.dto.PaginatedResponse
import com.ggc.fishingcopilot.fishingsession.rod.FishingRodService
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.CreateRodRequest
import com.ggc.fishingcopilot.fishingsession.rod.model.dto.RodResponse
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
        return ResponseEntity.ok(FishingSessionResponse(session.id, session.name, session.date))
    }

    //Create endpoint fishing-session/history that returns a list of FishingSessionResponse paginated
    @GetMapping("/fishing-session/history")
    @Operation(summary = "Get fishing session history", description = "Returns a list of past fishing sessions")
    fun history(
        @RequestHeader("sessionId") sessionId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): PaginatedResponse<FishingSessionResponse> {
        return service.getPaginatedSessions(sessionId, page, size)
    }

    @GetMapping("/fishing-session/current")
    @Operation(summary = "Get current fishing session", description = "Returns current IN_PROGRESS session if exists")
    fun current(@RequestHeader("sessionId") sessionId: UUID): ResponseEntity<FishingSessionResponse> {
        val session = service.getCurrent(sessionId)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(FishingSessionResponse(session.id, session.name, session.date))
    }

    @PostMapping("/fishing-session/close")
    @Operation(summary = "Close current fishing session", description = "Close the current IN_PROGRESS session")
    fun close(@RequestHeader("sessionId") sessionId: UUID): ResponseEntity<Void> {
        service.close(sessionId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/fishing-session/{sessionId}/rods")
    @Operation(summary = "List fishing rods", description = "List rods for the session")
    fun listRods(
        @RequestHeader("sessionId") sessionId: UUID,
        @PathVariable("sessionId") fishingSessionId: Int
    ): ResponseEntity<List<RodResponse>> {
        val rods = rodService.getRods(sessionId, fishingSessionId)
        return ResponseEntity.ok(rods)
    }

    @PostMapping("/fishing-session/{sessionId}/rods")
    @Operation(summary = "Add fishing rod", description = "Add a fishing rod to the session")
    fun addRod(
        @RequestHeader("sessionId") sessionId: UUID,
        @PathVariable("sessionId") fishingSessionId: Int,
        @RequestBody req: CreateRodRequest
    ): ResponseEntity<RodResponse> {
        val rod = rodService.addRod(sessionId, fishingSessionId, req.name)
        return ResponseEntity.ok(RodResponse(rod.id, rod.name, 0))
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

    @PostMapping("/fishing-session/{sessionId}/rod/{rodId}/fish")
    @Operation(summary = "Add fish", description = "Add a fish to the rod")
    fun addFish(
        @RequestHeader("sessionId") sessionId: UUID,
        @PathVariable("sessionId") fishingSessionId: Int,
        @PathVariable rodId: Int
    ): ResponseEntity<RodResponse> {
        val resp = rodService.addFish(sessionId, fishingSessionId, rodId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(resp)
    }

    @DeleteMapping("/fishing-session/{sessionId}/rod/{rodId}/fish")
    @Operation(summary = "Remove last fish", description = "Remove the last fish from the rod")
    fun removeFish(
        @RequestHeader("sessionId") sessionId: UUID,
        @PathVariable("sessionId") fishingSessionId: Int,
        @PathVariable rodId: Int
    ): ResponseEntity<RodResponse> {
        val resp = rodService.removeFish(sessionId, fishingSessionId, rodId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(resp)
    }
}
