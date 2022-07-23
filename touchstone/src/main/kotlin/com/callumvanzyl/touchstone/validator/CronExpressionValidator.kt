package com.callumvanzyl.touchstone.validator

import com.callumvanzyl.touchstone.annotation.ValidCronExpression
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import org.quartz.CronExpression

class CronExpressionValidator : ConstraintValidator<ValidCronExpression, String?> {

    override fun isValid(value: String?, context: ConstraintValidatorContext) = value == null || CronExpression.isValidExpression(value)
}
