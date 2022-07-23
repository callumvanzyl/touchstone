package com.callumvanzyl.touchstone.repository

import com.callumvanzyl.touchstone.model.TestSchematic
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TestSchematicRepository : MongoRepository<TestSchematic, String>
