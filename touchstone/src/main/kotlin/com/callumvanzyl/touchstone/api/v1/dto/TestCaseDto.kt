package com.callumvanzyl.touchstone.api.v1.dto

import com.callumvanzyl.touchstone.api.v1.util.OptionalConstraintGroup
import com.callumvanzyl.touchstone.model.TestCase
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel(value = "TestCase", description = "A test case")
data class TestCaseDto(

    @field:ApiModelProperty(value = "The unique ID of the test case", example = "5fed3eeafb00812bddd68192", required = true)
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val id: String? = null,

    @field:ApiModelProperty(value = "The human-readable name of the test case", example = "My Test Case", required = true)
    @field:NotNull(groups = [OptionalConstraintGroup::class])
    @field:Size(message = "The name of the test case must be between 5 and 32 characters in length", min = 5, max = 32)
    val name: String?,

    @field:ApiModelProperty(value = "The TouchstoneScript that will be run by this test case")
    val script: String?
)

fun TestCase.toDto() = TestCaseDto(
    id = id,
    name = name,
    script = script
)

fun TestCaseDto.toPersistable() = TestCase(
    id = id,
    name = name!!,
    script = script ?: ""
)

fun TestCaseDto.merge(other: TestCaseDto) = this.copy(
    name = other.name ?: this.name,
    script = other.script ?: this.script
)
