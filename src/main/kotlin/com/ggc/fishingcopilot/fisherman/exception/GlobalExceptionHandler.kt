package com.ggc.fishingcopilot.fisherman.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException::class)
    fun handleUsernameExists(ex: UsernameAlreadyExistsException) =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ex.message)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.message)

    @ExceptionHandler(FishermanNotFoundException::class)
    fun handleFishermanNotFound(ex: FishermanNotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)

    @ExceptionHandler(SessionNotFoundException::class)
    fun handleSessionNotFound(ex: SessionNotFoundException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.message)

    @ExceptionHandler(IllegalAccessException::class)
    fun handleSessionNotFound(ex: IllegalAccessException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.message)
}