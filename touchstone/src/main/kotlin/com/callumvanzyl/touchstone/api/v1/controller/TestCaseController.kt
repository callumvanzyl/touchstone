package com.callumvanzyl.touchstone.api.v1.controller

import com.callumvanzyl.touchstone.api.v1.ApiPaths
import com.callumvanzyl.touchstone.api.v1.dto.TestCaseDto
import com.callumvanzyl.touchstone.api.v1.dto.merge
import com.callumvanzyl.touchstone.api.v1.dto.toDto
import com.callumvanzyl.touchstone.api.v1.dto.toPersistable
import com.callumvanzyl.touchstone.api.v1.exception.TouchstoneApiException
import com.callumvanzyl.touchstone.api.v1.response.TouchstoneApiResponse
import com.callumvanzyl.touchstone.service.TestCaseService
import com.callumvanzyl.touchstone.util.Outcome
import com.callumvanzyl.touchstone.util.toHttpStatus
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.validation.Valid
import javax.validation.groups.Default
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Api(value = "/cases", tags = ["Cases"], description = "Manage test cases")
@RestController
@Validated
class TestCaseController(
    private val testCaseService: TestCaseService
) {

    @ApiOperation(value = "Retrieve a test case by ID")
    @GetMapping(ApiPaths.V1_MANIPULATE_TEST_CASE_BY_ID)
    fun getById(
        @ApiParam(value = "The ID of the test case")
        @PathVariable
        caseId: String
    ): ResponseEntity<TouchstoneApiResponse.Success<TestCaseDto>> =
        when (val result = testCaseService.findById(caseId)) {
            is Outcome.Success -> ResponseEntity.ok().body(TouchstoneApiResponse.Success(result.data.toDto()))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Create a new test case")
    @PostMapping(ApiPaths.V1_TEST_CASE)
    fun create(
        @ApiParam(name = "testCase", value = "The initial values assigned to the new test case")
        @Valid
        @RequestBody
        testCaseDto: TestCaseDto
    ): ResponseEntity<TouchstoneApiResponse.Success<TestCaseDto>> =
        when (val result = testCaseService.save(testCaseDto.toPersistable())) {
            is Outcome.Success -> ResponseEntity.status(HttpStatus.CREATED).body(TouchstoneApiResponse.Success(result.data.toDto()))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Update a test case by ID")
    @PatchMapping(ApiPaths.V1_MANIPULATE_TEST_CASE_BY_ID)
    fun updateById(
        @ApiParam(value = "The ID of the test case")
        @PathVariable
        caseId: String,
        @ApiParam(name = "testCase", value = "The new values assigned to the test case")
        @Validated(Default::class)
        @RequestBody
        testCaseDto: TestCaseDto
    ): ResponseEntity<TouchstoneApiResponse.Success<TestCaseDto>> =
        when (val caseResult = testCaseService.findById(caseId)) {
            is Outcome.Success -> {
                when (val saveResult = testCaseService.saveById(caseId, caseResult.data.toDto().merge(testCaseDto).toPersistable())) {
                    is Outcome.Success -> ResponseEntity.ok().body(TouchstoneApiResponse.Success(saveResult.data.toDto()))
                    is Outcome.Error -> throw TouchstoneApiException(saveResult.toHttpStatus(), saveResult.reason)
                }
            }
            is Outcome.Error -> throw TouchstoneApiException(caseResult.toHttpStatus(), caseResult.reason)
        }
}
