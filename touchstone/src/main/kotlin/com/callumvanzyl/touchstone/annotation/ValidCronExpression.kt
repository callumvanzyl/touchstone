package com.callumvanzyl.touchstone.annotation

import com.callumvanzyl.touchstone.validator.CronExpressionValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CronExpressionValidator::class])
annotation class ValidCronExpression(
    val message: String = "Invalid cron expression provided",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Payload>> = []
)
