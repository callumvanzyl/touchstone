package com.callumvanzyl.touchstone.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("test_schematics")
data class TestSchematic(

    @Id
    val id: String? = null,

    @Field("name")
    val name: String,

    @Field("case_ids")
    val caseIds: List<String> = emptyList(),

    @Field("schedule")
    val schedule: String? = null,

    @Field("script_variables")
    val scriptVariables: Map<String, String> = emptyMap(),

    @Field("deployment_plans")
    val deploymentPlans: List<DeploymentPlan>? = null
)
