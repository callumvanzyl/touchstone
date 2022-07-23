package com.callumvanzyl.touchstone.repository

import com.callumvanzyl.touchstone.model.TestCase
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TestCaseRepository : MongoRepository<TestCase, String>
