package com.callumvanzyl.touchstone.api.v1.controller

import com.callumvanzyl.touchstone.api.v1.ApiPaths
import com.callumvanzyl.touchstone.api.v1.exception.TouchstoneApiException
import com.callumvanzyl.touchstone.api.v1.response.TouchstoneApiResponse
import com.callumvanzyl.touchstone.service.ImageService
import com.callumvanzyl.touchstone.service.ResourcesService
import com.callumvanzyl.touchstone.util.Outcome
import com.callumvanzyl.touchstone.util.toHttpStatus
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import javax.servlet.http.HttpServletResponse
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Api(value = "/resources", tags = ["Resources"], description = "Manage resources")
@RestController
class ResourcesController(
    private val imageService: ImageService,
    private val resourcesService: ResourcesService
) {

    @ApiOperation(value = "Upload a new database dump")
    @PostMapping(ApiPaths.V1_RESOURCES_DUMP)
    fun uploadDump(
        @ApiParam(hidden = true)
        @RequestHeader("Authorization") token: String,
        @RequestPart("file")
        file: MultipartFile
    ): ResponseEntity<TouchstoneApiResponse.Success<String>> =
        when (val result = resourcesService.handleDumpUpload(file)) {
            is Outcome.Success -> ResponseEntity.status(HttpStatus.CREATED).body(TouchstoneApiResponse.Success(result.data))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Get screenshot by ID")
    @GetMapping(value = [ApiPaths.V1_RESOURCES_SCREENSHOT], produces = [MediaType.IMAGE_PNG_VALUE])
    fun getScreenshot(
        @ApiParam(hidden = true)
        @RequestHeader("Authorization") token: String,
        @ApiParam(value = "The ID of the image to retrieve")
        @PathVariable
        name: String
    ): ResponseEntity<ByteArray> =
        when (val result = imageService.getImage(name)) {
            is Outcome.Success -> ResponseEntity.ok(IOUtils.toByteArray(result.data))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }

    @ApiOperation(value = "Get a run report")
    @GetMapping(value = [ApiPaths.V1_RESOURCES_RUN_REPORT], produces = ["application/zip"])
    fun getRunReport(
        @ApiParam(hidden = true)
        @RequestHeader("Authorization") token: String,
        @ApiParam(value = "The ID of the run to report on")
        @PathVariable
        runId: String,
        response: HttpServletResponse
    ): ResponseEntity<ByteArray> {
        response.addHeader("Content-Disposition", "attachment; filename=\"report-$runId.zip\"")
        when (val result = resourcesService.getRunReport(runId)) {
            is Outcome.Success -> return ResponseEntity.ok(IOUtils.toByteArray(result.data.inputStream()))
            is Outcome.Error -> throw TouchstoneApiException(result.toHttpStatus(), result.reason)
        }
    }
}
