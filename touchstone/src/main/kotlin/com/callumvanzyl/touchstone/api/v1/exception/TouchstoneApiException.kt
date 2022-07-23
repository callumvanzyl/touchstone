package com.callumvanzyl.touchstone.api.v1.exception

import java.lang.RuntimeException
import org.springframework.http.HttpStatus

class TouchstoneApiException(val status: HttpStatus, message: String) : RuntimeException(message)
