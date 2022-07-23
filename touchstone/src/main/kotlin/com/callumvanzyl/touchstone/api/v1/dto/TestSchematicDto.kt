package com.callumvanzyl.touchstone.api.v1.dto

import com.callumvanzyl.touchstone.annotation.ValidCronExpression
import com.callumvanzyl.touchstone.api.v1.util.OptionalConstraintGroup
import com.callumvanzyl.touchstone.model.TestSchematic
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel(value = "TestSchematic", description = "A test schematic")
data class TestSchematicDto(

    @field:ApiModelProperty(value = "The unique ID of the test schematic", example = "5fed3eeafb00812bddd68192", required = true)
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val id: String? = null,

    @field:ApiModelProperty(value = "The human-readable name of the test schematic", example = "My Test Schematic", required = true)
    @field:NotNull(groups = [OptionalConstraintGroup::class])
    @field:Size(message = "The name of the test schematic must be between 5 and 32 characters in length", min = 5, max = 32)
    val name: String?,

    @field:ApiModelProperty(value = "The IDs of test cases assigned to this test schematic", required = true)
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val caseIds: List<String>?,

    @field:ApiModelProperty(value = "A cron expression denoting how frequently the test schematic is executed", example = "0 0 0/1 1/1 * ? *")
    @field:ValidCronExpression
    val schedule: String?,

    @field:ApiModelProperty(value = "The variables passed into each TouchstoneScript", example = "{\"variable-one\": \"Example 1\", \"variable-two\": \"Example 2\", \"variable-three\": \"Example 3\"}")
    val scriptVariables: Map<String, String>?,

    @field:ApiModelProperty(value = "TODO")
    @field:Valid
    val deploymentPlans: List<DeploymentPlanDto>?
)

fun TestSchematic.toDto() = TestSchematicDto(
    id = id,
    name = name,
    caseIds = caseIds,
    schedule = schedule,
    scriptVariables = scriptVariables,
    deploymentPlans = deploymentPlans?.map { it.toDto() }
)

fun TestSchematicDto.toPersistable() = TestSchematic(
    id = id,
    name = name!!,
    caseIds = caseIds ?: emptyList(),
    schedule = schedule,
    scriptVariables = scriptVariables ?: emptyMap(),
    deploymentPlans = deploymentPlans?.map { it.toPersistable() }
)

fun TestSchematicDto.merge(other: TestSchematicDto) = this.copy(
    name = other.name ?: this.name,
    schedule = other.schedule ?: this.schedule,
    scriptVariables = other.scriptVariables ?: this.scriptVariables
    // containerPlans = this.containerPlans.mapIndexed{ index, plan ->   }
    // dockerSettings = this.dockerSettings?.let { a -> other.dockerSettings?.let { b -> a.merge(b) } ?: a } ?: other.dockerSettings
)
