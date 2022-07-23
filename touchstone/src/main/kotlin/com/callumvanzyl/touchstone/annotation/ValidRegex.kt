package com.callumvanzyl.touchstone.annotation

import com.callumvanzyl.touchstone.validator.RegexValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [RegexValidator::class])
annotation class ValidRegex(
    val message: String = "Invalid regex provided",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Payload>> = []
)
