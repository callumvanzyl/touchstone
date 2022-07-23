package com.callumvanzyl.touchstone.service

import com.callumvanzyl.touchstone.exception.PersistableNotFoundException
import com.callumvanzyl.touchstone.model.TestCase
import com.callumvanzyl.touchstone.repository.TestCaseRepository
import com.callumvanzyl.touchstone.util.Outcome
import org.springframework.stereotype.Service

interface TestCaseService {
    fun findById(id: String): Outcome<TestCase>
    fun save(testCase: TestCase): Outcome<TestCase>
    fun saveById(id: String, testCase: TestCase): Outcome<TestCase>
}

@Service("TestCaseService")
class TestCaseServiceImpl(
    private val testCaseRepository: TestCaseRepository
) : TestCaseService {

    override fun findById(id: String) = testCaseRepository.findById(id).orElse(null)?.let { case ->
        Outcome.Success(case)
    } ?: Outcome.Error(reason = "Test case $id could not be found", exception = PersistableNotFoundException())

    override fun save(testCase: TestCase) =
        Outcome.Success(testCaseRepository.save(testCase))

    override fun saveById(id: String, testCase: TestCase): Outcome<TestCase> =
        Outcome.Success(testCaseRepository.save(testCase.copy(id = id)))
}
