package com.ggc.fishingcopilot.fisherman.service

import com.ggc.fishingcopilot.fisherman.FishermanRepository
import com.ggc.fishingcopilot.fisherman.exception.*
import com.ggc.fishingcopilot.fisherman.model.entity.Fisherman
import com.ggc.fishingcopilot.session.UserSessionRepository
import com.ggc.fishingcopilot.session.model.entity.UserSession
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FishermanService(
  private val repository: FishermanRepository,
  private val sessionRepository: UserSessionRepository
) {

  private val encoder = BCryptPasswordEncoder()

  fun register(username: String, password: String, question: String, answer: String) {
    if (repository.findByUsername(username) != null) {
      throw UsernameAlreadyExistsException()
    }
    val fisherman = Fisherman(
      username = username,
      password = encoder.encode(password),
      secretQuestion = question,
      secretAnswer = encoder.encode(answer)
    )
    repository.save(fisherman)
  }

  fun signIn(login: String, password: String): UUID {
    val fisherman = repository.findByUsername(login) ?: throw BadRequestException()
    if (!encoder.matches(password, fisherman.password)) {
      throw InvalidCredentialsException()
    }
    val session = UserSession(fisherman = fisherman)
    sessionRepository.save(session)
    return session.sessionId
  }

  fun checkSession(sessionId: UUID): Fisherman {
    return sessionRepository.findById(sessionId).orElseThrow { SessionNotFoundException() }.fisherman
  }

  fun logout(sessionId: UUID) {
    if (!sessionRepository.existsById(sessionId)) {
      throw SessionNotFoundException()
    }
    sessionRepository.deleteById(sessionId)
  }

  fun secretQuestion(login: String, answer: String?): String? {
    val fisherman = repository.findByUsername(login) ?: throw FishermanNotFoundException()
    if (answer == null) {
      //Get the question
      return fisherman.secretQuestion
    } else {
      //Check the answer
      if (!encoder.matches(answer, fisherman.secretAnswer)) {
        throw WrongAnswerException()
      } else {
        return "OK"
      }
    }
  }

  fun resetPassword(login: String, answer: String, newPassword: String) {
    val fisherman = repository.findByUsername(login) ?: throw FishermanNotFoundException()
    if (!encoder.matches(answer, fisherman.secretAnswer)) {
      throw WrongAnswerException()
    }
    fisherman.password = encoder.encode(newPassword)
    repository.save(fisherman)
  }
}