package com.ggc.fishingcopilot.fisherman.controller

import com.ggc.fishingcopilot.fisherman.model.dto.PasswordUpdateRequest
import com.ggc.fishingcopilot.fisherman.model.dto.QuestionResponse
import com.ggc.fishingcopilot.fisherman.model.dto.RegisterRequest
import com.ggc.fishingcopilot.fisherman.model.dto.SignInRequest
import com.ggc.fishingcopilot.fisherman.service.FishermanService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Fisherman", description = "Operations related to fisherman accounts")
class FishermanController(private val fishermanService: FishermanService) {

  @PostMapping("/register")
  @Operation(
    summary = "Register a new fisherman",
    description = "Creates a new fisherman account with a secret question and answer"
  )
  fun register(@RequestBody req: RegisterRequest): ResponseEntity<Void> {
    fishermanService.register(req.username, req.password, req.question, req.answer)
    return ResponseEntity.ok().build()
  }

  @PostMapping("/sign-in")
  @Operation(summary = "Sign in", description = "Authenticates a fisherman using username and password")
  fun signIn(@RequestBody req: SignInRequest): ResponseEntity<Void> {
    fishermanService.signIn(req.login, req.password)
    return ResponseEntity.ok().build()
  }

  @GetMapping("/fisherman/{login}/secret-question")
  @Operation(
    summary = "Get or validate secret question",
    description = "Returns the fisherman's secret question or checks the provided answer"
  )
  fun secretQuestion(
    @PathVariable login: String,
    @RequestParam(required = false) answer: String?
  ): ResponseEntity<Any> {
    //If fishermanService.secretQuestion(login, answer) return OK then return NO_CONTENT that means the answer is correct
    //Else return the question with 200
    return fishermanService.secretQuestion(login, answer).let {
      if (it == "OK") {
        ResponseEntity.noContent().build()
      } else {
        ResponseEntity.ok(QuestionResponse(it!!))
      }
    }
  }

  @PatchMapping("/fisherman/{login}/password")
  @Operation(summary = "Update password", description = "Updates the password after validating the secret answer")
  fun updatePassword(
    @PathVariable login: String,
    @RequestParam answer: String,
    @RequestBody req: PasswordUpdateRequest
  ): ResponseEntity<Void> {
    fishermanService.resetPassword(login, answer, req.newPassword)
    return ResponseEntity.ok().build()
  }
}