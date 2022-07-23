package com.callumvanzyl.touchstone.api.v1

object ApiPaths {

    const val V1_TEST_SCHEMATIC = "api/v1/schematics"
    const val V1_MANIPULATE_TEST_SCHEMATIC_BY_ID = "api/v1/schematics/{schematicId}"
    const val V1_MANIPULATE_TEST_CASES_IN_TEST_SCHEMATIC_BY_IDS = "api/v1/schematics/{schematicId}/cases/{caseId}"

    const val V1_TEST_CASE = "api/v1/cases"
    const val V1_MANIPULATE_TEST_CASE_BY_ID = "api/v1/cases/{caseId}"

    const val V1_TEST_RUNNER = "api/v1/runner"
    const val V1_MANIPULATE_TEST_RUN_BY_ID = "api/v1/runner/{runId}"
    const val V1_TEST_RUNNER_QUEUE = "api/v1/runner/queue"
    const val V1_MANIPULATE_TEST_RUNNER_QUEUE_BY_SCHEMATIC_ID = "api/v1/runner/queue/{schematicId}"
    const val V1_TEST_RUNNER_RESULTS = "api/v1/runner/results"
    const val V1_GET_TEST_RESULTS_BY_SCHEMATIC_ID = "api/v1/runner/results/{schematicId}"

    const val V1_RESOURCES_SCREENSHOT = "api/v1/resources/screenshots/{name}"
    const val V1_RESOURCES_RUN_REPORT = "api/v1/resources/report/{runId}"
    const val V1_RESOURCES_DUMP = "api/v1/resources/dumps"

    const val V1_LOGIN = "api/v1/login"
    const val V1_REGISTER = "api/v1/register"
}
