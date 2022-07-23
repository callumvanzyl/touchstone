package com.callumvanzyl.touchstone.validator

import com.callumvanzyl.touchstone.annotation.ValidRegex
import java.util.regex.Pattern
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class RegexValidator : ConstraintValidator<ValidRegex, String?> {

    override fun isValid(value: String?, context: ConstraintValidatorContext) = value == null || try { Pattern.compile(value); true } catch (_: Exception) { false }
}
