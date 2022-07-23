package com.callumvanzyl.touchstone.api.v1.controller

import com.callumvanzyl.touchstone.api.v1.ApiPaths
import com.callumvanzyl.touchstone.api.v1.ApiPaths.V1_TEST_RUNNER
import com.callumvanzyl.touchstone.api.v1.dto.TestRunDto
import com.callumvanzyl.touchstone.api.v1.dto.toDto
import com.callumvanzyl.touchstone.api.v1.dto.toPersistable
import com.callumvanzyl.touchstone.api.v1.exception.TouchstoneApiException
import com.callumvanzyl.touchstone.api.v1.response.TouchstoneApiResponse
import com.callumvanzyl.touchstone.service.TestRunService
import com.callumvanzyl.touchstone.service.UserService
import com.callumvanzyl.touchstone.util.Outcome
import com.callumvanzyl.touchstone.util.toHttpStatus
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@Api(value = "/runs", tags = ["Runs"], description = "Manage test runs")
@RestController
@Validated
class TestRunController(
    private val testRunService: TestRunService,
    private val userService: UserService
) {

    @ApiOperation(value = "Get all test runs")
    @GetMapping(ApiPaths.V1_TEST_RUNNER)
    fun getAllTestRuns(
        @ApiParam(hidden = true)
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<TouchstoneApiResponse.Success<List<TestRunDto>>> =
        when (val result = testRunService.getAllTestRuns()) {
            is Outcome.Success -> ResponseEntity.ok(TouchstoneApiResponse.Success(result.data.map { it.toDto() }))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Retrieve a test run by ID")
    @GetMapping(ApiPaths.V1_MANIPULATE_TEST_RUN_BY_ID)
    fun getById(
        @ApiParam(value = "The ID of the test run")
        @PathVariable
        runId: String
    ): ResponseEntity<TouchstoneApiResponse.Success<TestRunDto>> =
        when (val result = testRunService.findById(runId)) {
            is Outcome.Success -> ResponseEntity.ok().body(TouchstoneApiResponse.Success(result.data.toDto()))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Get test runs in the queue")
    @GetMapping(ApiPaths.V1_TEST_RUNNER_QUEUE)
    fun getTestRunsInQueue(
        @ApiParam(hidden = true)
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<TouchstoneApiResponse.Success<List<TestRunDto>>> =
        when (val result = testRunService.getTestRunsInQueue()) {
            is Outcome.Success -> ResponseEntity.ok(TouchstoneApiResponse.Success(result.data.map { it.toDto() }))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Add a new test run to the queue")
    @PostMapping(ApiPaths.V1_TEST_RUNNER_QUEUE)
    fun addTestRunToQueue(
        @ApiParam(hidden = true)
        @RequestHeader("Authorization") token: String,
        @ApiParam(name = "testRun", value = "The initial values assigned to the new test run")
        @Valid
        @RequestBody
        testRunDto: TestRunDto
    ): ResponseEntity<TouchstoneApiResponse.Success<TestRunDto>> =
        when (val usernameResult = userService.getUsernameFromToken(token.replace("Bearer ", ""))) {
            is Outcome.Success -> {
                when (val queueResult = testRunService.addToQueue(testRunDto.copy(creator = usernameResult.data).toPersistable())) {
                    is Outcome.Success -> ResponseEntity.status(HttpStatus.CREATED).body(TouchstoneApiResponse.Success(queueResult.data.toDto()))
                    is Outcome.Error -> throw TouchstoneApiException(queueResult.toHttpStatus(), queueResult.reason)
                }
            }
            is Outcome.Error -> throw TouchstoneApiException(usernameResult.toHttpStatus(), usernameResult.reason)
        }
}
