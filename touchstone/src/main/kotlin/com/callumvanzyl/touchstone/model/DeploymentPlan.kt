package com.callumvanzyl.touchstone.model

data class DeploymentPlan(

    val name: String,
    val type: ContainerType,
    val port: Int?,
    val environment: Map<String, String> = emptyMap(),

    val databaseSettings: DatabaseSettings?,

    val imageName: String,
    val imageTagRegex: String
)

data class DatabaseSettings(
    val driver: DatabaseDriver,
    val dumpName: String
)

enum class ContainerType {
    APPLICATION,
    DATABASE
}

enum class DatabaseDriver {
    MONGO
}
