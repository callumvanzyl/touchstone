package com.callumvanzyl.touchstone.api.v1.dto

import com.callumvanzyl.touchstone.annotation.ValidRegex
import com.callumvanzyl.touchstone.api.v1.util.OptionalConstraintGroup
import com.callumvanzyl.touchstone.model.ContainerType
import com.callumvanzyl.touchstone.model.DeploymentPlan
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel(value = "DeploymentPlan", description = "TODO")
data class DeploymentPlanDto(
    @field:ApiModelProperty(value = "The human-readable name of the deployment plan", example = "My Deployment Plan", required = true)
    @field:NotNull(groups = [OptionalConstraintGroup::class])
    @field:Size(message = "The name of the deployment plan must be between 5 and 32 characters in length", min = 5, max = 32)
    val name: String?,

    @field:ApiModelProperty(allowableValues = "APPLICATION, DATABASE", required = true)
    @field:NotNull(groups = [OptionalConstraintGroup::class])
    val type: String?,

    @field:ApiModelProperty(value = "The port to expose to the Touchstone container network, if left blank this will be automatically set to the first port exposed in the Docker image")
    val port: Int?,

    @field:ApiModelProperty(value = "The environment variables passed into each test container", example = "{\"variable-one\": \"Example 1\", \"variable-two\": \"Example 2\", \"variable-three\": \"Example 3\"}")
    val environment: Map<String, String>?,

    @field:ApiModelProperty(value = "TODO")
    @field:Valid
    val databaseSettings: DatabaseSettingsDto?,

    @field:ApiModelProperty(value = "The name of the Docker image", required = true)
    @field:NotNull(groups = [OptionalConstraintGroup::class])
    val imageName: String?,

    @field:ApiModelProperty(value = "TODO", example = "TODO", required = true)
    @field:NotNull(groups = [OptionalConstraintGroup::class])
    @field:ValidRegex
    val imageTagRegex: String?
)

fun DeploymentPlan.toDto() = DeploymentPlanDto(
    name = name,
    type = type.toString(),
    port = port,
    environment = environment,
    databaseSettings = databaseSettings?.toDto(),
    imageName = imageName,
    imageTagRegex = imageTagRegex
)

fun DeploymentPlanDto.toPersistable() = DeploymentPlan(
    name = name!!,
    type = ContainerType.valueOf(type!!),
    port = port,
    environment = environment ?: emptyMap(),
    databaseSettings = databaseSettings?.toPersistable(),
    imageName = imageName!!,
    imageTagRegex = imageTagRegex!!
)

fun DeploymentPlanDto.merge(other: DeploymentPlanDto) = this.copy()
