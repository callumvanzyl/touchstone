package com.callumvanzyl.touchstone.api.v1.dto

import com.callumvanzyl.touchstone.api.v1.util.OptionalConstraintGroup
import com.callumvanzyl.touchstone.model.DatabaseDriver
import com.callumvanzyl.touchstone.model.DatabaseSettings
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull

@ApiModel(value = "DatabaseSettings", description = "TODO")
data class DatabaseSettingsDto(

    @field:ApiModelProperty(allowableValues = "MONGO", required = true)
    @field:NotNull(groups = [OptionalConstraintGroup::class])
    val driver: String?,

    @field:ApiModelProperty(value = "TODO", example = "example.bson.gz", required = true)
    @field:NotNull(groups = [OptionalConstraintGroup::class])
    val dumpName: String?
)

fun DatabaseSettings.toDto() = DatabaseSettingsDto(
    driver = driver.toString(),
    dumpName = dumpName
)

fun DatabaseSettingsDto.toPersistable() = DatabaseSettings(
    driver = DatabaseDriver.valueOf(driver!!),
    dumpName = dumpName!!
)

fun DatabaseSettingsDto.merge(other: DatabaseSettingsDto) = this.copy()
