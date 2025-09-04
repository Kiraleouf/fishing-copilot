package com.ggc.fishingcopilot.fisherman.model.dto

data class RegisterRequest(
  val username: String,
  val password: String,
  val question: String,
  val answer: String
)

data class SignInRequest(
  val login: String,
  val password: String
)

data class QuestionResponse(
  val question: String
)

data class PasswordUpdateRequest(
  val newPassword: String
)