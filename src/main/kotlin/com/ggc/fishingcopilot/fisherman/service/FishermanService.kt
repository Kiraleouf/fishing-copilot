package com.ggc.fishingcopilot.fisherman.service

import com.ggc.fishingcopilot.fisherman.FishermanRepository
import com.ggc.fishingcopilot.fisherman.exception.*
import com.ggc.fishingcopilot.fisherman.model.entity.Fisherman
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class FishermanService(private val repository: FishermanRepository) {

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

  fun signIn(login: String, password: String) {
    val fisherman = repository.findByUsername(login) ?: throw BadRequestException()
    if (!encoder.matches(password, fisherman.password)) {
      throw InvalidCredentialsException()
    }
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