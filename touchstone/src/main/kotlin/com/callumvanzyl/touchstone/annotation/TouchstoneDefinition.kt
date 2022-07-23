package com.callumvanzyl.touchstone.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TouchstoneDefinition(
    val name: String = "",
    val description: String = ""
)
