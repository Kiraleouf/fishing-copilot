package com.example.demo.fisherman

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@RestController
@Tag(name = "Fisherman", description = "Operations related to fisherman accounts")
class FishermanController(private val repository: FishermanRepository) {

    private val encoder = BCryptPasswordEncoder()

    data class RegisterRequest(
        val username: String,
        val password: String,
        val question: String,
        val answer: String
    )

    @PostMapping("/register")
    @Operation(summary = "Register a new fisherman", description = "Creates a new fisherman account with a secret question and answer")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Void> {
        if (repository.findByUsername(req.username) != null) {
            return ResponseEntity.status(409).build()
        }
        val fisherman = Fisherman(
            username = req.username,
            password = encoder.encode(req.password),
            secretQuestion = req.question,
            secretAnswer = encoder.encode(req.answer)
        )
        repository.save(fisherman)
        return ResponseEntity.ok().build()
    }

    data class SignInRequest(val login: String, val password: String)

    @PostMapping("/sign-in")
    @Operation(summary = "Sign in", description = "Authenticates a fisherman using username and password")
    fun signIn(@RequestBody req: SignInRequest): ResponseEntity<Void> {
        val fisherman = repository.findByUsername(req.login) ?: return ResponseEntity.status(401).build()
        return if (encoder.matches(req.password, fisherman.password)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(401).build()
        }
    }

    data class QuestionResponse(val question: String)

    @GetMapping("/fisherman/{login}/secret-question")
    @Operation(summary = "Get or validate secret question", description = "Returns the fisherman's secret question or checks the provided answer")
    fun secretQuestion(
        @PathVariable login: String,
        @RequestParam(required = false) answer: String?
    ): ResponseEntity<Any> {
        val fisherman = repository.findByUsername(login) ?: return ResponseEntity.notFound().build()
        return if (answer == null) {
            ResponseEntity.ok(QuestionResponse(fisherman.secretQuestion))
        } else {
            if (encoder.matches(answer, fisherman.secretAnswer)) {
                ResponseEntity.ok().build()
            } else {
                ResponseEntity.status(401).build()
            }
        }
    }

    data class PasswordUpdateRequest(val newPassword: String)

    @PatchMapping("/fisherman/{login}/password")
    @Operation(summary = "Update password", description = "Updates the password after validating the secret answer")
    fun updatePassword(
        @PathVariable login: String,
        @RequestParam answer: String,
        @RequestBody req: PasswordUpdateRequest
    ): ResponseEntity<Void> {
        val fisherman = repository.findByUsername(login) ?: return ResponseEntity.notFound().build()
        if (!encoder.matches(answer, fisherman.secretAnswer)) {
            return ResponseEntity.status(401).build()
        }
        fisherman.password = encoder.encode(req.newPassword)
        repository.save(fisherman)
        return ResponseEntity.ok().build()
    }
}
