package com.callumvanzyl.touchstone.model

import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("test_runs")
data class TestRun(

    @Id
    val id: String? = null,

    @Field("schematic_id")
    val schematicId: String,

    @Field("status")
    val status: TestRunStatus = TestRunStatus.UNKNOWN,

    @Field("creator")
    val creator: String,

    @Field("queued_time")
    val queuedTime: Instant? = null,

    @Field("start_time")
    val startTime: Instant? = null,

    @Field("end_time")
    val endTime: Instant? = null,

    @Field("additional_script_variables")
    val additionalScriptVariables: Map<String, String> = emptyMap(),

    @Field("steps_waiting")
    val stepsWaiting: Int = 0,

    @Field("steps_passed")
    val stepsPassed: Int = 0,

    @Field("steps_failed")
    val stepsFailed: Int = 0,

    @Field("cases")
    val cases: List<TestCaseResult> = emptyList()
)

data class TestCaseResult(

    val id: String = UUID.randomUUID().toString().takeLast(8),
    val name: String,
    val script: String,
    val scenarios: List<TestScenarioResult> = emptyList()
)

data class TestScenarioResult(

    val name: String,
    val steps: List<TestStepResult> = emptyList()
)

data class TestStepResult(

    val name: String,
    val endTime: Instant? = null,
    val status: TestStepStatus,
    val detail: String? = null,
    val screenshot: String? = null
)

enum class TestRunStatus {
    ERROR,
    FINISHED,
    QUEUED,
    PREPARING,
    RUNNING,
    UNKNOWN
}

enum class TestStepStatus {
    WAITING,
    PASSED,
    SKIPPED,
    FAILED
}
