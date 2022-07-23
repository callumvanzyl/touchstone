package com.callumvanzyl.touchstone.api.v1.response

import com.callumvanzyl.touchstone.api.v1.exception.TouchstoneApiException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ErrorHandler {

    // Unknown exceptions, these are unhandled exceptions that can be thrown anywhere
    @ExceptionHandler(value = [Exception::class])
    fun genericExceptionHandler(exception: Exception): ResponseEntity<TouchstoneApiResponse.Error<String>> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                TouchstoneApiResponse.Error(
                    status = HttpStatus.INTERNAL_SERVER_ERROR,
                    reason = "An unknown error occurred",
                    data = "Error #${System.currentTimeMillis()}"
                )
            )

    // Known exceptions, these are handled exceptions that are thrown by the API after some application logic reports an erroneous outcome
    @ExceptionHandler(value = [TouchstoneApiException::class])
    fun touchstoneApiExceptionHandler(exception: TouchstoneApiException): ResponseEntity<TouchstoneApiResponse.Error<String>> =
        ResponseEntity
            .status(exception.status)
            .body(
                TouchstoneApiResponse.Error(
                    status = exception.status,
                    reason = exception.message ?: ""
                )
            )

    // These exceptions most often occur when the user passes in some data that Jackson fails to interpret
    @ExceptionHandler(value = [MethodArgumentTypeMismatchException::class])
    fun methodArgumentTypeMismatchExceptionHandler(exception: MethodArgumentTypeMismatchException): ResponseEntity<TouchstoneApiResponse.Error<String>> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                TouchstoneApiResponse.Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = "A mapping error occurred",
                    data = "The value of ${exception.name} is not castable to ${exception.requiredType}"
                )
            )

    // These exceptions most often occur when the user passes in some data that violates a data constraint set within the API
    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun methodArgumentTypeMismatchExceptionHandler(exception: MethodArgumentNotValidException): ResponseEntity<TouchstoneApiResponse.Error<List<ValidationErrorPair>>> {
        val data = exception.bindingResult.allErrors.map {
            ValidationErrorPair(if (it is FieldError) it.field else "request", it.defaultMessage ?: "")
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                TouchstoneApiResponse.Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = "A validation error occurred",
                    data = data
                )
            )
    }

    companion object {

        data class ValidationErrorPair(val field: String, val message: String)
    }
}
