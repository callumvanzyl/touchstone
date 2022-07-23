package com.callumvanzyl.touchstone.api.v1.response

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class TouchstoneApiResponse<out T>(val success: Boolean) {
    data class Success<out T : Any>(val data: T) : TouchstoneApiResponse<T>(true)
    data class Error<out T : Any>(val status: HttpStatus, val reason: String, val data: T? = null) : TouchstoneApiResponse<T>(false)
}
