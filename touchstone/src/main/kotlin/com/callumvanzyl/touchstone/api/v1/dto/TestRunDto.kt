package com.callumvanzyl.touchstone.api.v1.dto

import com.callumvanzyl.touchstone.model.TestRun
import com.callumvanzyl.touchstone.model.TestRunStatus
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.Instant
import javax.validation.constraints.NotNull

@ApiModel(value = "TestRun", description = "A test run")
data class TestRunDto(

    @field:ApiModelProperty(value = "The unique ID of the test run", example = "5fed3eeafb00812bddd68192", required = true)
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val id: String? = null,

    @field:ApiModelProperty(value = "The ID of the test schematic associated with this test run", example = "5fed3eeafb00812bddd68192", required = true)
    @field:NotNull
    val schematicId: String?,

    @field:ApiModelProperty(allowableValues = "ERROR, FINISHED, QUEUED, PREPARING, RUNNING, UNKNOWN", required = true)
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val status: String?,

    @field:ApiModelProperty(value = "The username of the user that triggered this test run")
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val creator: String?,

    @field:ApiModelProperty(value = "The time that this test run was added to the queue", example = "2021-01-01T12:00:00.000Z")
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val queuedTime: String?,

    @field:ApiModelProperty(value = "The time that this test run started", example = "2021-01-01T12:00:00.000Z")
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val startTime: String?,

    @field:ApiModelProperty(value = "The time that this test run finished", example = "2021-01-01T12:00:00.000Z")
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val endTime: String?,

    @field:ApiModelProperty(value = "The additional variables passed into each TouchstoneScript alongside and taking priority over the schematic variables", example = "{\"variable-one\": \"Example 1\", \"variable-two\": \"Example 2\", \"variable-three\": \"Example 3\"}")
    val additionalScriptVariables: Map<String, String>?,

    @field:ApiModelProperty(value = "The number of steps that are waiting to run")
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val stepsWaiting: String?,

    @field:ApiModelProperty(value = "The number of steps that have ran and passed")
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val stepsPassed: String?,

    @field:ApiModelProperty(value = "The number of steps that have ran and failed")
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val stepsFailed: String?
)

fun TestRun.toDto() = TestRunDto(
    id = id,
    schematicId = schematicId,
    status = status.toString(),
    creator = creator,
    queuedTime = queuedTime?.toString(),
    startTime = startTime?.toString(),
    endTime = endTime?.toString(),
    additionalScriptVariables = additionalScriptVariables,
    stepsWaiting = stepsWaiting.toString(),
    stepsPassed = stepsPassed.toString(),
    stepsFailed = stepsFailed.toString()
)

fun TestRunDto.toPersistable() = TestRun(
    id = id,
    schematicId = schematicId!!,
    status = TestRunStatus.valueOf(status ?: "UNKNOWN"),
    creator = creator!!,
    queuedTime = try { queuedTime?.let { Instant.parse(it) } } catch (err: Exception) { null },
    startTime = try { startTime?.let { Instant.parse(it) } } catch (err: Exception) { null },
    endTime = try { endTime?.let { Instant.parse(it) } } catch (err: Exception) { null },
    additionalScriptVariables = additionalScriptVariables ?: emptyMap(),
    stepsWaiting = stepsWaiting?.toInt() ?: 0,
    stepsPassed = stepsPassed?.toInt() ?: 0,
    stepsFailed = stepsFailed?.toInt() ?: 0
)
