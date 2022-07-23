package com.callumvanzyl.touchstone.service

import com.callumvanzyl.touchstone.automation.util.WebDriverManager
import com.callumvanzyl.touchstone.exception.PersistableNotFoundException
import com.callumvanzyl.touchstone.model.TestCaseResult
import com.callumvanzyl.touchstone.model.TestRun
import com.callumvanzyl.touchstone.model.TestRunStatus
import com.callumvanzyl.touchstone.repository.TestRunRepository
import com.callumvanzyl.touchstone.util.Outcome
import io.cucumber.core.cli.Main
import java.io.File
import java.time.Instant
import javax.annotation.PostConstruct
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.system.exitProcess
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

interface TestRunService {
    fun save(testRun: TestRun): Outcome<TestRun>
    fun findById(id: String): Outcome<TestRun>
    fun addToQueue(testRun: TestRun): Outcome<TestRun>
    fun getAllTestRuns(): Outcome<List<TestRun>>
    fun getTestRunsInQueue(): Outcome<List<TestRun>>
}

@Service("TestRunService")
class TestRunServiceImpl(
    @Value("\${chrome.web-driver-path}") private val chromeWebDriverPath: String,
    @Value("\${docker.enabled}") private val dockerEnabled: Boolean,
    @Value("\${touchstone.data-path}") private val touchstoneDataPath: String,

    private val orchestrationService: OrchestrationService?,
    private val testCaseService: TestCaseService,
    private val testSchematicService: TestSchematicService,

    private val testRunRepository: TestRunRepository
) : TestRunService {

    @PostConstruct
    private fun postConstruct() {
        // If Touchstone is shut down unexpectedly then any running tests will be stuck in an invalid state, putting
        // these stuck tests into a finished state fixes the issue.
        (testRunRepository.findByStatus(TestRunStatus.RUNNING) + testRunRepository.findByStatus(TestRunStatus.PREPARING)).let { fix ->
            if (fix.isNotEmpty()) {
                logger.warn("Fixing disjointed test runs, these can occur after forcefully terminating Touchstone")
                fix.forEach { schematic ->
                    testRunRepository.save(schematic.copy(status = TestRunStatus.ERROR))
                }
            }
        }

        // If Chrome is enabled then confirm the presence of the ChromeDriver
        if (!File(chromeWebDriverPath).exists()) {
            logger.error("The Chrome web driver cannot not be found at $chromeWebDriverPath")
            exitProcess(1)
        }
        System.setProperty("webdriver.chrome.driver", chromeWebDriverPath)
    }

    @ExperimentalPathApi
    @Scheduled(fixedDelay = 1000)
    private fun scheduled() =
        testRunRepository.findByStatus(TestRunStatus.QUEUED).let { runs ->
            runs.filter { it.queuedTime != null }.minByOrNull { it.queuedTime!! }?.let { run ->
                when (val preparation = prepareTestRun(run)) {
                    is Outcome.Success -> {
                        testRunRepository.save(preparation.data)
                        when (val executed = executeTestRun(preparation.data)) {
                            is Outcome.Success -> {
                                testRunRepository.save(executed.data)
                                finishTestRun(executed.data)
                            }
                            is Outcome.Error -> {
                                testRunRepository.save(
                                    executed.data?.let {
                                        executed.data.copy(status = TestRunStatus.ERROR)
                                    } ?: preparation.data.copy(status = TestRunStatus.ERROR)
                                )
                                logger.warn("Test run ${run.id} failed to execute: ${executed.reason}")
                            }
                        }
                    }
                    is Outcome.Error -> {
                        testRunRepository.save(run.copy(status = TestRunStatus.ERROR))
                        logger.warn("Test run ${run.id} failed to prepare: ${preparation.reason}")
                    }
                }
            }
        }

    private fun prepareTestRun(testRun: TestRun): Outcome<TestRun> {
        var updated = testRun.copy(status = TestRunStatus.PREPARING)
        logger.info("Test run ${testRun.id} is preparing")

        val schematicResult = testSchematicService.findById(testRun.schematicId)
        when (schematicResult) {
            is Outcome.Success -> {}
            is Outcome.Error -> return Outcome.Error(reason = schematicResult.reason)
        }

        if (schematicResult.data.deploymentPlans != null) {
            if (dockerEnabled) {
                when (val result = orchestrationService!!.deployPlans(schematicResult.data.deploymentPlans, testRun)) {
                    is Outcome.Success -> updated = testRunRepository.save(result.data)
                    is Outcome.Error -> return Outcome.Error(reason = result.reason)
                }
            } else {
                return Outcome.Error(reason = "Test schematics containing a deployment plan can not run when Docker integration is not enabled")
            }
        }
        schematicResult.data.caseIds.forEach { caseId ->
            (testCaseService.findById(caseId) as? Outcome.Success)?.let { caseResult ->
                var formattedScript = caseResult.data.script
                (schematicResult.data.scriptVariables.keys + updated.additionalScriptVariables.keys).associateWith {
                    setOf(
                        schematicResult.data.scriptVariables[it],
                        updated.additionalScriptVariables[it]
                    ).filterNotNull().last()
                }.forEach { variable ->
                    formattedScript = formattedScript.replace("&${variable.key}&", variable.value)
                }
                updated = testRunRepository.save(
                    updated.copy(
                        cases = updated.cases + TestCaseResult(
                            name = caseResult.data.name,
                            script = formattedScript
                        )
                    )
                )
            }
        }
        return Outcome.Success(updated)
    }

    @ExperimentalPathApi
    private fun executeTestRun(testRun: TestRun): Outcome<TestRun> {
        var updated = testRun
        try {
            updated = testRunRepository.save(updated.copy(status = TestRunStatus.RUNNING, startTime = Instant.now()))
            logger.info("Test run ${testRun.id} is running")
            testRun.cases.forEach { case ->
                WebDriverManager.reset()
                val file = kotlin.io.path.createTempFile(suffix = ".feature")
                file.writeText(case.script)
                System.setProperty("RUN_ID", testRun.id!!)
                System.setProperty("CASE_ID", case.id)
                System.setProperty("DATA_PATH", touchstoneDataPath)
                val argv = arrayOf(
                    file.toAbsolutePath().toString(),
                    "-g", "com.callumvanzyl.touchstone.automation.dsl.steps",
                    "-p", "com.callumvanzyl.touchstone.automation.reporter.TouchstoneReporter"
                )
                Main.run(argv, Thread.currentThread().contextClassLoader)
                file.deleteIfExists()
            }
            return Outcome.Success(testRunRepository.findById(testRun.id!!).get())
        } catch (err: Exception) {
            testRunRepository.save(updated.copy(status = TestRunStatus.ERROR))
            return Outcome.Error(err.localizedMessage)
        }
    }

    private fun finishTestRun(testRun: TestRun) {
        testRunRepository.save(testRun.copy(status = TestRunStatus.FINISHED, endTime = Instant.now()))
        if (dockerEnabled) {
            orchestrationService!!.generateLogs(testRun)
            orchestrationService.tidy()
        }
        logger.info("Test run ${testRun.id} has finished")
    }

    override fun save(testRun: TestRun): Outcome<TestRun> =
        Outcome.Success(testRunRepository.save(testRun))

    override fun findById(id: String) = testRunRepository.findById(id).orElse(null)?.let { case ->
        Outcome.Success(case)
    } ?: Outcome.Error(reason = "Test run $id could not be found", exception = PersistableNotFoundException())

    override fun addToQueue(testRun: TestRun): Outcome<TestRun> =
        when (val result = testSchematicService.findById(testRun.schematicId)) {
            is Outcome.Success -> save(testRun.copy(status = TestRunStatus.QUEUED, queuedTime = Instant.now()))
            is Outcome.Error -> Outcome.Error(reason = result.reason, exception = result.exception)
        }

    override fun getAllTestRuns(): Outcome<List<TestRun>> =
        Outcome.Success(testRunRepository.findAll())

    override fun getTestRunsInQueue(): Outcome<List<TestRun>> =
        Outcome.Success(testRunRepository.findAll().filter { it.status == TestRunStatus.QUEUED })

    companion object {

        val logger: Logger = LoggerFactory.getLogger(TestRunService::class.java)
    }
}
