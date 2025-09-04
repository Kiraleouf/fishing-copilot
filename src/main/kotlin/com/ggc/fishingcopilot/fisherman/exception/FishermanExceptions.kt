package com.ggc.fishingcopilot.fisherman.exception

class UsernameAlreadyExistsException : RuntimeException("Username already exists")
class InvalidCredentialsException : RuntimeException("Invalid credentials")
class FishermanNotFoundException : RuntimeException("Fisherman not found")
class BadRequestException : RuntimeException("Wrong request")
class UnauthorizedException : RuntimeException("Unauthorized")
class WrongAnswerException : RuntimeException("Wrong answer")
class SessionNotFoundException : RuntimeException("Session not found")