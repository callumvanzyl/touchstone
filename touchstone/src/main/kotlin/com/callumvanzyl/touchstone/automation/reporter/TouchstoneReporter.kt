package com.callumvanzyl.touchstone.automation.reporter

import com.callumvanzyl.touchstone.automation.util.WebDriverManager
import com.callumvanzyl.touchstone.model.TestScenarioResult
import com.callumvanzyl.touchstone.model.TestStepResult
import com.callumvanzyl.touchstone.model.TestStepStatus
import com.callumvanzyl.touchstone.service.ImageService
import com.callumvanzyl.touchstone.service.TestRunService
import com.callumvanzyl.touchstone.util.Outcome
import com.callumvanzyl.touchstone.util.SpringApplicationContextContainer
import io.cucumber.plugin.EventListener
import io.cucumber.plugin.event.EventPublisher
import io.cucumber.plugin.event.PickleStepTestStep
import io.cucumber.plugin.event.Status
import io.cucumber.plugin.event.TestCaseFinished
import io.cucumber.plugin.event.TestCaseStarted
import io.cucumber.plugin.event.TestStepFinished
import java.io.File
import java.time.Instant
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.logging.LogType

/**
 * A Cucumber plugin used to report test results to Touchstone
 */
class TouchstoneReporter(
    private val caseId: String = System.getProperty("CASE_ID"),
    private val runId: String = System.getProperty("RUN_ID"),
    private val touchstoneDataPath: String = System.getProperty("DATA_PATH")
) : EventListener {

    init {
        System.clearProperty("CASE_ID")
        System.clearProperty("RUN_ID")
        System.clearProperty("DATA_PATH")
    }

    override fun setEventPublisher(publisher: EventPublisher) {
        publisher.registerHandlerFor(TestCaseStarted::class.java) { event: TestCaseStarted -> testCaseStartedHandler(event) }
        publisher.registerHandlerFor(TestStepFinished::class.java) { event: TestStepFinished -> testStepFinishedHandler(event) }
        publisher.registerHandlerFor(TestCaseFinished::class.java) { event: TestCaseFinished -> testCaseFinishedHandler(event) }
    }

    private fun testCaseStartedHandler(event: TestCaseStarted) =
        (testRunService.findById(runId) as? Outcome.Success)?.let { runResult ->
            var totalSteps = 0
            testRunService.save(
                runResult.data.copy(
                    cases = runResult.data.cases.mapIndexed { _, case ->
                        if (case.id == caseId) {
                            case.copy(
                                scenarios = case.scenarios + TestScenarioResult(
                                    name = event.testCase.name,
                                    steps = event.testCase.testSteps.map { step ->
                                        totalSteps++
                                        val pickleStep = step as PickleStepTestStep
                                        TestStepResult(
                                            name = "${pickleStep.step.keyword}${pickleStep.step.text}",
                                            status = TestStepStatus.WAITING
                                        )
                                    }
                                )
                            )
                        } else {
                            case
                        }
                    },
                    stepsWaiting = totalSteps
                )
            )
        }

    private fun testCaseFinishedHandler(event: TestCaseFinished) {
        val destination = File("$touchstoneDataPath/logs/$runId")
        destination.mkdirs()
        val out = File(destination, "chrome-$caseId.txt")
        out.appendText("Chrome logs for run $runId when executing case $caseId \n")
        WebDriverManager.webDriver.manage().logs().get(LogType.BROWSER).forEach { out.appendText("${it.message}\n") }
    }

    private fun testStepFinishedHandler(event: TestStepFinished) =
        (testRunService.findById(runId) as? Outcome.Success)?.let { runResult ->
            val pickleStep = event.testStep as PickleStepTestStep
            Thread.sleep(500)
            testRunService.save(
                    runResult.data.copy(
                        cases = runResult.data.cases.mapIndexed { _, case ->
                            if (case.id == caseId) {
                                case.copy(
                                    scenarios = case.scenarios.mapIndexed { scenarioIndex, scenario ->
                                        if (scenarioIndex == case.scenarios.size - 1) {
                                            scenario.copy(
                                                steps = scenario.steps.map { step ->
                                                    if (step.name == "${pickleStep.step.keyword}${pickleStep.step.text}") {
                                                        val image = (WebDriverManager.webDriver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
                                                        step.copy(
                                                            status = try { TestStepStatus.valueOf(event.result.status.toString()) } catch (err: Exception) { TestStepStatus.FAILED },
                                                            endTime = Instant.now(),
                                                            detail = if (event.result.status == Status.FAILED) { event.result.error.localizedMessage } else { null },
                                                            screenshot = when (imageService.storeImage(image)) {
                                                                is Outcome.Success -> image.name
                                                                is Outcome.Error -> null
                                                            }
                                                        )
                                                    } else {
                                                        step
                                                    }
                                                }
                                            )
                                        } else {
                                            scenario
                                        }
                                    }
                                )
                            } else {
                                case
                            }
                        },
                        stepsWaiting = runResult.data.stepsWaiting - 1,
                        stepsPassed = if (event.result.status == Status.PASSED) runResult.data.stepsPassed + 1 else runResult.data.stepsPassed,
                        stepsFailed = if (event.result.status != Status.PASSED) runResult.data.stepsFailed + 1 else runResult.data.stepsFailed
                    )
                )
        }

    companion object {

        val imageService = SpringApplicationContextContainer.context.getBean(ImageService::class.java)
        val testRunService = SpringApplicationContextContainer.context.getBean(TestRunService::class.java)
    }
}
