package com.callumvanzyl.touchstone.util

import com.callumvanzyl.touchstone.exception.InvalidCredentialsException
import com.callumvanzyl.touchstone.exception.InvalidOperationException
import com.callumvanzyl.touchstone.exception.PersistableNotFoundException
import java.lang.Exception
import org.springframework.http.HttpStatus

sealed class Outcome<out T> {
    data class Success<out T : Any>(val data: T) : Outcome<T>()
    data class Error<out T : Any>(val reason: String, val exception: Exception? = null, val data: T? = null) : Outcome<T>()
}

fun Outcome.Error<Any>.toHttpStatus(): HttpStatus = when (this.exception) {
    is InvalidCredentialsException -> HttpStatus.UNAUTHORIZED
    is InvalidOperationException -> HttpStatus.BAD_REQUEST
    is PersistableNotFoundException -> HttpStatus.NOT_FOUND
    else -> HttpStatus.INTERNAL_SERVER_ERROR
}
