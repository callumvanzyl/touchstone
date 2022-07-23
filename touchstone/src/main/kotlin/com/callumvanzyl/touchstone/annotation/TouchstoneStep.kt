package com.callumvanzyl.touchstone.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class TouchstoneStep(
    val name: String = "",
    val description: String = "",
    val example: String = ""
)
