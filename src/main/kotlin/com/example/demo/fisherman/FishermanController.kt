package com.example.demo.fisherman

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import jakarta.servlet.http.HttpSession

@RestController
class FishermanController(private val repository: FishermanRepository) {

    private val encoder = BCryptPasswordEncoder()

    data class RegisterRequest(
        val login: String,
        val password: String,
        val question: String,
        val answer: String
    )

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Void> {
        if (repository.findByLogin(req.login) != null) {
            return ResponseEntity.status(409).build()
        }
        val fisherman = Fisherman(
            login = req.login,
            password = encoder.encode(req.password),
            secretQuestion = req.question,
            secretAnswer = encoder.encode(req.answer)
        )
        repository.save(fisherman)
        return ResponseEntity.ok().build()
    }

    data class SignInRequest(val login: String, val password: String)

    @PostMapping("/sign-in")
    fun signIn(@RequestBody req: SignInRequest, session: HttpSession): ResponseEntity<Void> {
        val fisherman = repository.findByLogin(req.login) ?: return ResponseEntity.status(401).build()
        return if (encoder.matches(req.password, fisherman.password)) {
            session.setAttribute("user", fisherman.login)
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(401).build()
        }
    }

    data class QuestionResponse(val question: String)

    @GetMapping("/fisherman/{login}/secret-question")
    fun secretQuestion(
        @PathVariable login: String,
        @RequestParam(required = false) answer: String?
    ): ResponseEntity<Any> {
        val fisherman = repository.findByLogin(login) ?: return ResponseEntity.notFound().build()
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
    fun updatePassword(
        @PathVariable login: String,
        @RequestParam answer: String,
        @RequestBody req: PasswordUpdateRequest
    ): ResponseEntity<Void> {
        val fisherman = repository.findByLogin(login) ?: return ResponseEntity.notFound().build()
        if (!encoder.matches(answer, fisherman.secretAnswer)) {
            return ResponseEntity.status(401).build()
        }
        fisherman.password = encoder.encode(req.newPassword)
        repository.save(fisherman)
        return ResponseEntity.ok().build()
    }
}
