package com.callumvanzyl.touchstone.api.v1.dto

import com.callumvanzyl.touchstone.api.v1.util.OptionalConstraintGroup
import com.callumvanzyl.touchstone.model.User
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ApiModel(value = "User", description = "A user")
data class UserDto(

    @field:ApiModelProperty(value = "The unique ID of the user", example = "5fed3eeafb00812bddd68192", required = true)
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val id: String? = null,

    @field:ApiModelProperty(value = "", example = "", required = true)
    @field:NotNull
    @field:Size(message = "Your username must be between 3 and 16 characters in length", min = 3, max = 16, groups = [OptionalConstraintGroup::class])
    val username: String?,

    @field:ApiModelProperty(value = "", example = "", required = true)
    @field:NotNull
    @field:Size(message = "Your password must be between 5 and 32 characters in length", min = 5, max = 32, groups = [OptionalConstraintGroup::class])
    val password: String?,

    @field:ApiModelProperty(value = "", required = true)
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val enabled: Boolean?
)

fun User.toDto() = UserDto(
    id = id,
    username = username,
    password = null,
    enabled = enabled
)

fun UserDto.toPersistable() = User(
    id = id,
    username = username!!,
    password = password!!,
    enabled = enabled ?: false
)
