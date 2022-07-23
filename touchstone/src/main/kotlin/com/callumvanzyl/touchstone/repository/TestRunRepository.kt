package com.callumvanzyl.touchstone.repository

import com.callumvanzyl.touchstone.model.TestRun
import com.callumvanzyl.touchstone.model.TestRunStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TestRunRepository : MongoRepository<TestRun, String> {

    @Query("{ 'status' : ?0 }")
    fun findByStatus(status: TestRunStatus): List<TestRun>
}
