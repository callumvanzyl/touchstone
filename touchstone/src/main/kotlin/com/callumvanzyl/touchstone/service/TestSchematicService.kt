package com.callumvanzyl.touchstone.service

import com.callumvanzyl.touchstone.exception.InvalidOperationException
import com.callumvanzyl.touchstone.exception.PersistableNotFoundException
import com.callumvanzyl.touchstone.model.TestSchematic
import com.callumvanzyl.touchstone.repository.TestSchematicRepository
import com.callumvanzyl.touchstone.util.Outcome
import javax.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

interface TestSchematicService {
    fun findById(id: String): Outcome<TestSchematic>
    fun save(testSchematic: TestSchematic): Outcome<TestSchematic>
    fun saveById(id: String, testSchematic: TestSchematic): Outcome<TestSchematic>
    fun addTestCaseToTestSchematicByIds(schematicId: String, caseId: String): Outcome<String>
    fun removeTestCaseFromTestSchematicByIds(schematicId: String, caseId: String): Outcome<String>
}

@Service("TestSchematicService")
class TestSchematicServiceImpl(
    @Value("\${docker.enabled}") private val dockerEnabled: Boolean,

    private val testCaseService: TestCaseService,

    private val testSchematicRepository: TestSchematicRepository
) : TestSchematicService {

    @PostConstruct
    private fun postConstruct() {
        if (!dockerEnabled && testSchematicRepository.findAll().any { it.deploymentPlans != null }) {
            logger.warn("Docker integration is disabled, test schematics containing a deployment plan will fail to run")
        }
    }

    override fun findById(id: String) = testSchematicRepository.findById(id).orElse(null)?.let { schematic ->
        Outcome.Success(schematic)
    } ?: Outcome.Error(reason = "Test schematic $id could not be found", exception = PersistableNotFoundException())

    override fun save(testSchematic: TestSchematic) =
        Outcome.Success(testSchematicRepository.save(testSchematic))

    override fun saveById(id: String, testSchematic: TestSchematic): Outcome<TestSchematic> =
        Outcome.Success(testSchematicRepository.save(testSchematic.copy(id = id)))

    override fun addTestCaseToTestSchematicByIds(schematicId: String, caseId: String): Outcome<String> =
        when (val schematicResult = findById(schematicId)) {
            is Outcome.Success -> {
                when (val caseResult = testCaseService.findById(caseId)) {
                    is Outcome.Success -> {
                        if (schematicResult.data.caseIds.contains(caseId)) {
                            Outcome.Error(reason = "Test schematic ${schematicResult.data.id} already includes test case ${caseResult.data.id}", exception = InvalidOperationException())
                        } else {
                            testSchematicRepository.save(schematicResult.data.copy(caseIds = schematicResult.data.caseIds.plus(caseResult.data.id!!)))
                            Outcome.Success("Test case ${caseResult.data.id} was added to test schematic ${schematicResult.data.id}")
                        }
                    }
                    is Outcome.Error -> Outcome.Error(reason = caseResult.reason, exception = caseResult.exception)
                }
            }
            is Outcome.Error -> Outcome.Error(reason = schematicResult.reason, exception = schematicResult.exception)
        }

    override fun removeTestCaseFromTestSchematicByIds(schematicId: String, caseId: String): Outcome<String> =
        when (val schematicResult = findById(schematicId)) {
            is Outcome.Success -> {
                when (val caseResult = testCaseService.findById(caseId)) {
                    is Outcome.Success -> {
                        if (schematicResult.data.caseIds.contains(caseId)) {
                            testSchematicRepository.save(schematicResult.data.copy(caseIds = schematicResult.data.caseIds.filter { it != caseId }))
                            Outcome.Success("Test case ${caseResult.data.id} was removed from test schematic ${schematicResult.data.id}")
                        } else {
                            Outcome.Error(reason = "Test schematic ${schematicResult.data.id} does not include test case ${caseResult.data.id}", exception = InvalidOperationException())
                        }
                    }
                    is Outcome.Error -> Outcome.Error(reason = caseResult.reason, exception = caseResult.exception)
                }
            }
            is Outcome.Error -> Outcome.Error(reason = schematicResult.reason, exception = schematicResult.exception)
        }

    companion object {

        val logger: Logger = LoggerFactory.getLogger(TestSchematicService::class.java)
    }
}
