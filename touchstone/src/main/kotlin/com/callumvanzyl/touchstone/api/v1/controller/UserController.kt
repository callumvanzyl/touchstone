package com.callumvanzyl.touchstone.api.v1.controller

import com.callumvanzyl.touchstone.api.v1.ApiPaths
import com.callumvanzyl.touchstone.api.v1.dto.UserDto
import com.callumvanzyl.touchstone.api.v1.dto.toDto
import com.callumvanzyl.touchstone.api.v1.dto.toPersistable
import com.callumvanzyl.touchstone.api.v1.exception.TouchstoneApiException
import com.callumvanzyl.touchstone.api.v1.response.TouchstoneApiResponse
import com.callumvanzyl.touchstone.api.v1.util.OptionalConstraintGroup
import com.callumvanzyl.touchstone.service.UserService
import com.callumvanzyl.touchstone.util.Outcome
import com.callumvanzyl.touchstone.util.toHttpStatus
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.validation.groups.Default
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Api(value = "/users", tags = ["Users"], description = "Manage users")
@RestController
@Validated
class UserController(
    private val userService: UserService
) {

    @ApiOperation(value = "Register a new user")
    @PostMapping(ApiPaths.V1_REGISTER)
    fun register(
        @ApiParam(name = "user", value = "")
        @Validated(Default::class, OptionalConstraintGroup::class)
        @RequestBody
        userDto: UserDto
    ): ResponseEntity<TouchstoneApiResponse.Success<UserDto>> =
        when (val result = userService.register(userDto.toPersistable())) {
            is Outcome.Success -> ResponseEntity.status(HttpStatus.CREATED).body(TouchstoneApiResponse.Success(result.data.toDto()))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Login as an existing user")
    @PostMapping(ApiPaths.V1_LOGIN)
    fun login(
        @ApiParam(name = "user", value = "")
        @Validated(Default::class)
        @RequestBody
        userDto: UserDto
    ): ResponseEntity<TouchstoneApiResponse.Success<String>> =
        when (val result = userService.login(userDto.toPersistable())) {
            is Outcome.Success -> ResponseEntity.ok(TouchstoneApiResponse.Success(result.data))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }
}
