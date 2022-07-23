package com.callumvanzyl.touchstone.api.v1.controller

import com.callumvanzyl.touchstone.api.v1.ApiPaths
import com.callumvanzyl.touchstone.api.v1.dto.TestSchematicDto
import com.callumvanzyl.touchstone.api.v1.dto.merge
import com.callumvanzyl.touchstone.api.v1.dto.toDto
import com.callumvanzyl.touchstone.api.v1.dto.toPersistable
import com.callumvanzyl.touchstone.api.v1.exception.TouchstoneApiException
import com.callumvanzyl.touchstone.api.v1.response.TouchstoneApiResponse
import com.callumvanzyl.touchstone.api.v1.util.OptionalConstraintGroup
import com.callumvanzyl.touchstone.service.TestSchematicService
import com.callumvanzyl.touchstone.util.Outcome
import com.callumvanzyl.touchstone.util.toHttpStatus
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.validation.groups.Default
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Api(value = "/schematics", tags = ["Schematics"], description = "Manage test schematics")
@RestController
@Validated
class TestSchematicController(
    private val testSchematicService: TestSchematicService
) {

    @ApiOperation(value = "Retrieve a list of test schematics")
    @GetMapping(ApiPaths.V1_TEST_SCHEMATIC)
    fun get(
        @ApiParam(value = "The page number to retrieve")
        @RequestParam(name = "page", required = false, defaultValue = "0")
        page: Number,
        @ApiParam(value = "The number of test schematics on each page")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Number
    ): ResponseEntity<TouchstoneApiResponse.Success<TestSchematicDto>> =
        when (val result = testSchematicService.findById("0")) {
            is Outcome.Success -> ResponseEntity.status(HttpStatus.CREATED).body(TouchstoneApiResponse.Success(result.data.toDto()))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Retrieve a test schematic by ID")
    @GetMapping(ApiPaths.V1_MANIPULATE_TEST_SCHEMATIC_BY_ID)
    fun getById(
        @ApiParam(value = "The ID of the test schematic")
        @PathVariable
        schematicId: String
    ): ResponseEntity<TouchstoneApiResponse.Success<TestSchematicDto>> =
        when (val result = testSchematicService.findById(schematicId)) {
            is Outcome.Success -> ResponseEntity.ok().body(TouchstoneApiResponse.Success(result.data.toDto()))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Create a new test schematic")
    @PostMapping(ApiPaths.V1_TEST_SCHEMATIC)
    fun create(
        @ApiParam(name = "testSchematic", value = "The initial values assigned to the new test schematic")
        @Validated(Default::class, OptionalConstraintGroup::class)
        @RequestBody
        testSchematicDto: TestSchematicDto
    ): ResponseEntity<TouchstoneApiResponse.Success<TestSchematicDto>> =
        when (val result = testSchematicService.save(testSchematicDto.toPersistable())) {
            is Outcome.Success -> ResponseEntity.status(HttpStatus.CREATED).body(TouchstoneApiResponse.Success(result.data.toDto()))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Update a test schematic by ID")
    @PatchMapping(ApiPaths.V1_MANIPULATE_TEST_SCHEMATIC_BY_ID)
    fun updateById(
        @ApiParam(value = "The ID of the test schematic")
        @PathVariable
        schematicId: String,
        @ApiParam(name = "testSchematic", value = "The new values assigned to the test schematic")
        @Validated(Default::class)
        @RequestBody
        testSchematicDto: TestSchematicDto
    ): ResponseEntity<TouchstoneApiResponse.Success<TestSchematicDto>> =
        when (val schematicResult = testSchematicService.findById(schematicId)) {
            is Outcome.Success -> {
                when (val saveResult = testSchematicService.saveById(schematicId, schematicResult.data.toDto().merge(testSchematicDto).toPersistable())) {
                    is Outcome.Success -> ResponseEntity.ok().body(TouchstoneApiResponse.Success(saveResult.data.toDto()))
                    is Outcome.Error -> throw TouchstoneApiException(saveResult.toHttpStatus(), saveResult.reason)
                }
            }
            is Outcome.Error -> throw TouchstoneApiException(schematicResult.toHttpStatus(), schematicResult.reason)
        }

    @ApiOperation(value = "Add a test case to a test schematic by IDs")
    @PutMapping(ApiPaths.V1_MANIPULATE_TEST_CASES_IN_TEST_SCHEMATIC_BY_IDS)
    fun addTestCaseToTestSchematic(
        @ApiParam(value = "The ID of the test schematic")
        @PathVariable
        schematicId: String,
        @ApiParam(value = "The ID of the test case")
        @PathVariable
        caseId: String
    ): ResponseEntity<TouchstoneApiResponse.Success<String>> =
        when (val result = testSchematicService.addTestCaseToTestSchematicByIds(schematicId, caseId)) {
            is Outcome.Success -> ResponseEntity.ok().body(TouchstoneApiResponse.Success(result.data))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Remove a test case from a test schematic by IDs")
    @DeleteMapping(ApiPaths.V1_MANIPULATE_TEST_CASES_IN_TEST_SCHEMATIC_BY_IDS)
    fun removeTestCaseFromTestSchematic(
        @ApiParam(value = "The ID of the test schematic")
        @PathVariable
        schematicId: String,
        @ApiParam(value = "The ID of the test case")
        @PathVariable
        caseId: String
    ): ResponseEntity<TouchstoneApiResponse.Success<String>> =
        when (val result = testSchematicService.removeTestCaseFromTestSchematicByIds(schematicId, caseId)) {
            is Outcome.Success -> ResponseEntity.ok().body(TouchstoneApiResponse.Success(result.data))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }
}
